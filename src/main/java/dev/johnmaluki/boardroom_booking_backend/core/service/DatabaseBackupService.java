package dev.johnmaluki.boardroom_booking_backend.core.service;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class DatabaseBackupService {
    private final Dotenv dotenv;
    @Value("${backup.directory}")
    private String backupDirectory;

    private static final String USER_HOME_DIRECTORY = System.getProperty("user.home");
    private static final String BACKUP_SCRIPT_PATH = System.getProperty("user.dir") + "scripts/backup.sh";

    public void performDatabaseBackup() {
        String dbUser = dotenv.get("POSTGRES_DB_USER");
        String dbPassword = dotenv.get("POSTGRES_DB_PASSWORD");
        String dbName = dotenv.get("POSTGRES_DB");
        try {
            // Create timestamp for backup filename
            String timestamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            String backupFileName = USER_HOME_DIRECTORY + backupDirectory + File.separator + "db_backup_" + timestamp + ".sql";
            // Command for pg_dump
            String[] command = new String[]{
                    "pg_dump",
                    "-h", "0.0.0.0",
                    "-U", dbUser,
                    "-d", dbName,
                    "-Fc",
                    "-f", backupFileName,
            };

            // Create ProcessBuilder to execute the command
            ProcessBuilder processBuilder = new ProcessBuilder(command);

            // Set environment variable for password
            processBuilder.environment().put("PGPASSWORD", dbPassword);
            // Start the process
            Process process = processBuilder.start();
            // Capture and print standard output
            printStream(process.getInputStream(), "OUTPUT");

            // Capture and print error output
            printStream(process.getErrorStream(), "ERROR");

            // Wait for the process to complete
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                System.out.println("Backup completed: " + backupFileName);
                Path pathDir = Path.of(USER_HOME_DIRECTORY + backupDirectory);
                this.deleteOldBackups(pathDir);
            } else {
                System.err.println("Backup failed with exit code: " + exitCode);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Method to print output from a given InputStream
    private static void printStream(InputStream inputStream, String streamType) {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    System.out.println("[" + streamType + "] " + line);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    // Method to delete backups older than 48 hours
    private void deleteOldBackups(Path backupDirPath) {
        try (Stream<Path> files = Files.list( backupDirPath)) {
            long cutoffTime = System.currentTimeMillis() - (48 * 60 * 60 * 1000); // 48 hours in milliseconds

            files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql")) // Only consider .sql files
                    .filter(path -> path.toFile().lastModified() < cutoffTime)
                    .forEach(path -> {
                        try {
                            Files.delete(path);
                            System.out.println("Deleted old backup: " + path.getFileName());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
