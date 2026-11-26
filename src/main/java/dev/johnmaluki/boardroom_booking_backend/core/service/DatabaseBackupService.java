package dev.johnmaluki.boardroom_booking_backend.core.service;

import dev.johnmaluki.boardroom_booking_backend.config.JdbcUrlParser;
import dev.johnmaluki.boardroom_booking_backend.config.JdbcUrlParser.ConnectionTarget;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Nightly {@code pg_dump}, pointed at exactly the database the application itself connects to.
 *
 * <p>The host, port and database name all come from {@code app.datasource.url} via {@link
 * JdbcUrlParser} — the same property {@code DatabaseConfig} builds the live R2DBC connection
 * from — rather than from any separately-maintained host or database-name setting. That used to
 * be two independent sources of truth (a hardcoded backup host, and a database name read from a
 * second environment variable that was never guaranteed to match the one actually in use), and
 * this project's own {@code .env} has in fact drifted between them before: the backup process
 * would then be silently dumping the wrong target, or more likely failing outright, which is
 * about as "inaccurate" as a backup can get while still reporting as having run.
 *
 * <p>The dump is {@code -Fc}, PostgreSQL's custom format — compressed, and restorable with {@code
 * pg_restore --jobs} for a faster parallel restore, or to pull back a single table rather than
 * everything. That format is binary, not SQL text, so the output file is named {@code .dump}
 * rather than {@code .sql}: a previous version used the SQL extension on this exact binary
 * output, which would have misled anyone trying to restore it with {@code psql -f} instead of
 * {@code pg_restore}.
 */
@Slf4j
@Component
public class DatabaseBackupService {
  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
  private static final Duration RETENTION = Duration.ofHours(48);
  private static final Duration DUMP_TIMEOUT = Duration.ofMinutes(30);

  @Value("${app.datasource.url}")
  private String jdbcUrl;

  @Value("${app.datasource.username}")
  private String username;

  @Value("${app.datasource.password}")
  private String password;

  @Value("${backup.directory}")
  private String backupDirectory;

  public void performDatabaseBackup() {
    Path backupDir = resolveBackupDirectory(System.getProperty("user.home"), backupDirectory);
    if (!ensureDirectoryExists(backupDir)) {
      return;
    }

    ConnectionTarget target = JdbcUrlParser.parse(jdbcUrl);
    Path backupFile = backupDir.resolve("db_backup_" + LocalDateTime.now().format(TIMESTAMP_FORMAT) + ".dump");

    if (runPgDump(target, backupFile)) {
      log.info("Backup completed: {}", backupFile);
      deleteOldBackups(backupDir);
    }
  }

  /** The exact {@code pg_dump} argument list, pulled out so it can be tested without a shell. */
  static List<String> buildPgDumpCommand(ConnectionTarget target, String username, Path backupFile) {
    return List.of(
        "pg_dump",
        "-h", target.host(),
        "-p", String.valueOf(target.port()),
        "-U", username,
        "-d", target.database(),
        // No interactive password prompt under any circumstance: a scheduled job that falls
        // back to one just hangs forever, with nothing in the log to explain why. PGPASSWORD
        // below is the only credential path; if it's ever wrong or missing, this makes pg_dump
        // fail immediately instead.
        "--no-password",
        "-Fc",
        "-f", backupFile.toString());
  }

  /** Runs {@code pg_dump}, waiting at most {@link #DUMP_TIMEOUT} and validating the result. */
  private boolean runPgDump(ConnectionTarget target, Path backupFile) {
    List<String> command = buildPgDumpCommand(target, username, backupFile);
    ProcessBuilder processBuilder = new ProcessBuilder(command);
    processBuilder.environment().put("PGPASSWORD", password);

    ExecutorService streamReaders = Executors.newFixedThreadPool(2);
    try {
      Process process = processBuilder.start();
      Future<String> stdOut = streamReaders.submit(() -> readStream(process.getInputStream()));
      Future<String> stdErr = streamReaders.submit(() -> readStream(process.getErrorStream()));

      boolean finishedInTime = process.waitFor(DUMP_TIMEOUT.toMinutes(), TimeUnit.MINUTES);
      if (!finishedInTime) {
        process.destroyForcibly();
        log.error(
            "pg_dump against {}:{}/{} did not finish within {}; killed it. Target file: {}",
            target.host(), target.port(), target.database(), DUMP_TIMEOUT, backupFile);
        return false;
      }

      String out = getOrBlank(stdOut);
      String err = getOrBlank(stdErr);
      if (!out.isBlank()) {
        log.debug("pg_dump output: {}", out);
      }

      int exitCode = process.exitValue();
      if (exitCode != 0) {
        log.error(
            "pg_dump against {}:{}/{} exited with code {}: {}",
            target.host(), target.port(), target.database(), exitCode,
            err.isBlank() ? "(no error output)" : err);
        return false;
      }

      return validateBackupFile(backupFile);
    } catch (IOException e) {
      // Most commonly: the pg_dump binary itself is not on PATH.
      log.error("Could not start pg_dump: {}", e.getMessage());
      return false;
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      log.error("Interrupted while waiting for pg_dump to finish");
      return false;
    } finally {
      streamReaders.shutdownNow();
    }
  }

  /**
   * A zero exit code alone isn't proof of a usable backup — confirming the file actually exists
   * and has real content is what makes this "the backup succeeded" check trustworthy rather than
   * just "the process said it succeeded".
   */
  boolean validateBackupFile(Path backupFile) {
    try {
      if (!Files.isRegularFile(backupFile) || Files.size(backupFile) == 0) {
        log.error("pg_dump exited successfully but {} is missing or empty", backupFile);
        return false;
      }
      return true;
    } catch (IOException e) {
      log.error("Could not verify backup file {}: {}", backupFile, e.getMessage());
      return false;
    }
  }

  private String getOrBlank(Future<String> future) {
    try {
      return future.get();
    } catch (Exception e) {
      return "";
    }
  }

  private static String readStream(InputStream inputStream) {
    StringBuilder content = new StringBuilder();
    try (BufferedReader reader =
        new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
      String line;
      while ((line = reader.readLine()) != null) {
        content.append(line).append(System.lineSeparator());
      }
    } catch (IOException e) {
      // The process is already being torn down by the time this would matter; nothing useful
      // to do beyond not losing whatever was captured before the failure.
    }
    return content.toString();
  }

  /**
   * {@code backup.directory} is written with a leading {@code /} in every environment's
   * properties file (e.g. {@code /.kemri/backups/dev}), by convention rather than by anything
   * enforced in code; stripping it before resolving against {@code homeDir} is what keeps that
   * convention from silently producing an absolute path rooted outside the home directory
   * instead of the intended one under it.
   */
  static Path resolveBackupDirectory(String homeDir, String backupDirectoryProperty) {
    String relative =
        backupDirectoryProperty.startsWith("/")
            ? backupDirectoryProperty.substring(1)
            : backupDirectoryProperty;
    return Path.of(homeDir).resolve(relative);
  }

  /**
   * {@code pg_dump -f} cannot create its own output directory; on a machine where this path was
   * never created by hand, every scheduled backup would otherwise fail every single night with
   * nothing more specific in the log than "No such file or directory".
   */
  boolean ensureDirectoryExists(Path backupDir) {
    try {
      Files.createDirectories(backupDir);
      return true;
    } catch (IOException e) {
      log.error("Could not create backup directory {}: {}", backupDir, e.getMessage());
      return false;
    }
  }

  /** Deletes backups older than {@link #RETENTION}, run only after a successful backup. */
  void deleteOldBackups(Path backupDir) {
    long cutoffMillis = System.currentTimeMillis() - RETENTION.toMillis();
    try (Stream<Path> files = Files.list(backupDir)) {
      files
          .filter(Files::isRegularFile)
          .filter(path -> path.toString().endsWith(".dump"))
          .filter(path -> path.toFile().lastModified() < cutoffMillis)
          .forEach(this::deleteQuietly);
    } catch (IOException e) {
      log.warn("Could not list {} to clean up old backups: {}", backupDir, e.getMessage());
    }
  }

  private void deleteQuietly(Path path) {
    try {
      Files.delete(path);
      log.info("Deleted old backup: {}", path.getFileName());
    } catch (IOException e) {
      log.warn("Could not delete old backup {}: {}", path, e.getMessage());
    }
  }
}
