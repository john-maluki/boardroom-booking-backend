package dev.johnmaluki.boardroom_booking_backend;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Profile("dev")
public class DatabaseCleanup {
  private final JdbcTemplate jdbcTemplate;

  @PreDestroy
  public void dropDatabaseTables() {
    // You can use JdbcTemplate to execute a SQL command to drop tables
    jdbcTemplate.execute("DROP SCHEMA public CASCADE;");
    jdbcTemplate.execute("CREATE SCHEMA public;");
  }
}
