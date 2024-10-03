package dev.johnmaluki.boardroom_booking_backend.core.sql.backup;

import dev.johnmaluki.boardroom_booking_backend.core.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class BackupScheduler {
    private final DatabaseBackupService databaseBackupService;


    //@Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 24 hours
    @Scheduled(cron = "0 0 1 * * ?")
    public void scheduleDatabaseBackup() {
        databaseBackupService.performDatabaseBackup();
    }
}
