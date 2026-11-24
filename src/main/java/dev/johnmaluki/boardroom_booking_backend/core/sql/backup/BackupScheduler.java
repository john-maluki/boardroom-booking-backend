package dev.johnmaluki.boardroom_booking_backend.core.sql.backup;

import dev.johnmaluki.boardroom_booking_backend.core.service.DatabaseBackupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Nightly {@code pg_dump}.
 *
 * <p>{@code @Scheduled} runs on Spring's own task scheduler, never on a Netty event loop, so the
 * blocking process call is fine where it is.
 */
@Service
@RequiredArgsConstructor
public class BackupScheduler {
  private final DatabaseBackupService databaseBackupService;

  @Scheduled(cron = "0 0 1 * * ?")
  public void scheduleDatabaseBackup() {
    databaseBackupService.performDatabaseBackup();
  }
}
