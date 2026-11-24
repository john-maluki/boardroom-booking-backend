package dev.johnmaluki.boardroom_booking_backend;

import jakarta.annotation.PreDestroy;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Component;

/**
 * Drops the schema when a dev run ends, so the next start comes up against a clean database.
 *
 * <p>This is safe to re-enable now that {@code DatabaseConfig}'s {@code ConnectionFactoryInitializer}
 * recreates the schema from {@code src/main/resources/tables/*.sql} on every startup — it was
 * disabled for the period where nothing did that automatically, which would have left a dev
 * restart with a dropped schema and no tables. Failures are logged rather than thrown: the
 * process is already shutting down, and an un-dropped dev schema is harmless either way, since
 * every {@code CREATE TABLE} the startup scripts run is {@code IF NOT EXISTS}.
 */
@Component
@RequiredArgsConstructor
@Profile("dev")
@Slf4j
public class DatabaseCleanup {
  private static final Duration CLEANUP_TIMEOUT = Duration.ofSeconds(20);

  private final DatabaseClient databaseClient;

  @PreDestroy
  public void dropDatabaseTables() {
    try {
      databaseClient
          .sql("DROP SCHEMA public CASCADE")
          .then()
          .then(databaseClient.sql("CREATE SCHEMA public").then())
          .block(CLEANUP_TIMEOUT);
      log.info("Dropped and recreated the public schema");
    } catch (RuntimeException e) {
      log.warn("Could not reset the dev schema on shutdown: {}", e.getMessage());
    }
  }
}
