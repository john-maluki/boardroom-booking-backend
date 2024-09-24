package dev.johnmaluki.boardroom_booking_backend.core.sql.backup;

import dev.johnmaluki.boardroom_booking_backend.core.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackupScheduler {
    private final DatabaseBackupService databaseBackupService;



//    @Scheduled(fixedRate = 50 * 1 * 1 * 1000) // 10 hours
    @Scheduled(cron = "0 0 2 * * ?") // Schedule backup to run daily at 2 AM
    public void scheduleDatabaseBackup() {
        databaseBackupService.performDatabaseBackup();
    }
}
