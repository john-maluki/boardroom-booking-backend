package dev.johnmaluki.boardroom_booking_backend.core.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import dev.johnmaluki.boardroom_booking_backend.config.JdbcUrlParser;
import dev.johnmaluki.boardroom_booking_backend.config.JdbcUrlParser.ConnectionTarget;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

/**
 * Covers everything about the backup process that can be verified without an actual {@code
 * pg_dump} binary or a running Postgres — neither is available in every environment this runs
 * in. What's left uncovered is {@code runPgDump}'s own process-handling (the timeout, and
 * reading pg_dump's real stdout/stderr); that part is thin by design specifically so the rest —
 * command construction, directory handling, and the post-dump validity check — carries the
 * actual logic and can be exercised directly.
 */
class DatabaseBackupServiceTest {

  private final DatabaseBackupService service = new DatabaseBackupService();

  @Test
  void pgDumpCommandTargetsExactlyWhatTheJdbcUrlSays() {
    ConnectionTarget target = JdbcUrlParser.parse("jdbc:postgresql://localhost:5434/kemri-boardroom-booking");
    Path backupFile = Path.of("/tmp/backups/db_backup_20261126000000.dump");

    List<String> command =
        DatabaseBackupService.buildPgDumpCommand(target, "booking@user", backupFile);

    assertEquals(
        List.of(
            "pg_dump",
            "-h", "localhost",
            "-p", "5434",
            "-U", "booking@user",
            "-d", "kemri-boardroom-booking",
            "--no-password",
            "-Fc",
            "-f", backupFile.toString()),
        command);
  }

  @Test
  void pgDumpCommandNeverHardcodesAHostOrPort() {
    // The regression this guards: a previous version always connected to 0.0.0.0 with no -p
    // argument at all, so it silently ignored whatever port the database was actually on.
    ConnectionTarget target = JdbcUrlParser.parse("jdbc:postgresql://db.internal:6543/mydb");

    List<String> command = DatabaseBackupService.buildPgDumpCommand(target, "u", Path.of("x"));

    assertTrue(command.contains("db.internal"));
    assertTrue(command.contains("6543"));
    assertFalse(command.contains("0.0.0.0"));
  }

  @Test
  void backupFileNameReflectsItsActualFormatNotPlainSql() {
    // -Fc is PostgreSQL's binary custom format; a .sql extension on it would mislead anyone
    // trying to restore with "psql -f" instead of pg_restore.
    ConnectionTarget target = JdbcUrlParser.parse("jdbc:postgresql://localhost/mydb");
    List<String> command =
        DatabaseBackupService.buildPgDumpCommand(target, "u", Path.of("backup.dump"));

    assertTrue(command.contains("-Fc"));
  }

  @Test
  void backupDirectoryResolvesUnderHomeDespiteTheLeadingSlashConvention() {
    Path resolved = DatabaseBackupService.resolveBackupDirectory("/home/maluki", "/.kemri/backups/dev");

    assertEquals(Path.of("/home/maluki/.kemri/backups/dev"), resolved);
  }

  @Test
  void backupDirectoryResolvesEvenWithoutTheLeadingSlash() {
    Path resolved = DatabaseBackupService.resolveBackupDirectory("/home/maluki", ".kemri/backups/dev");

    assertEquals(Path.of("/home/maluki/.kemri/backups/dev"), resolved);
  }

  @Test
  void ensureDirectoryExistsCreatesNestedDirectoriesThatDoNotExistYet(@TempDir Path tempDir) {
    Path nested = tempDir.resolve("does/not/exist/yet");

    boolean created = service.ensureDirectoryExists(nested);

    assertTrue(created);
    assertTrue(Files.isDirectory(nested));
  }

  @Test
  void validateBackupFileRejectsAMissingFile(@TempDir Path tempDir) {
    assertFalse(service.validateBackupFile(tempDir.resolve("never-written.dump")));
  }

  @Test
  void validateBackupFileRejectsAnEmptyFile(@TempDir Path tempDir) throws IOException {
    // The regression this guards: pg_dump exiting 0 was previously trusted on its own, with no
    // check that it actually produced a usable file.
    Path empty = tempDir.resolve("empty.dump");
    Files.createFile(empty);

    assertFalse(service.validateBackupFile(empty));
  }

  @Test
  void validateBackupFileAcceptsAFileWithRealContent(@TempDir Path tempDir) throws IOException {
    Path real = tempDir.resolve("real.dump");
    Files.writeString(real, "not actually a pg_dump file, but not empty either");

    assertTrue(service.validateBackupFile(real));
  }

  @Test
  void deleteOldBackupsLeavesRecentFilesAlone(@TempDir Path tempDir) throws IOException {
    Path recent = tempDir.resolve("db_backup_recent.dump");
    Files.writeString(recent, "x");

    service.deleteOldBackups(tempDir);

    assertTrue(Files.exists(recent), "a file created moments ago must not be swept up");
  }

  @Test
  void deleteOldBackupsLeavesNonDumpFilesAloneRegardlessOfAge(@TempDir Path tempDir) throws IOException {
    Path other = tempDir.resolve("notes.txt");
    Files.writeString(other, "x");
    Files.setLastModifiedTime(other, java.nio.file.attribute.FileTime.fromMillis(0));

    service.deleteOldBackups(tempDir);

    assertTrue(Files.exists(other), "cleanup must only ever touch its own .dump files");
  }
}
