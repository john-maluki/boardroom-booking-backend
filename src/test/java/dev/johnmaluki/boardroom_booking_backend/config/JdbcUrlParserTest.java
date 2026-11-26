package dev.johnmaluki.boardroom_booking_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import dev.johnmaluki.boardroom_booking_backend.config.JdbcUrlParser.ConnectionTarget;
import org.junit.jupiter.api.Test;

/**
 * This parsing is now shared by {@code DatabaseConfig} (the live R2DBC connection) and {@code
 * DatabaseBackupService} ({@code pg_dump}) specifically so both always agree on where the
 * database actually is. A subtle bug here — the kind that only shows up on a URL shape the happy
 * path wasn't tried against — would silently point one of those two at the wrong database rather
 * than fail loudly, which is exactly the failure mode this class exists to rule out.
 */
class JdbcUrlParserTest {

  @Test
  void parsesHostPortAndDatabaseFromAFullUrl() {
    ConnectionTarget target =
        JdbcUrlParser.parse("jdbc:postgresql://localhost:5434/kemri-boardroom-booking");

    assertEquals("localhost", target.host());
    assertEquals(5434, target.port());
    assertEquals("kemri-boardroom-booking", target.database());
  }

  @Test
  void fallsBackToTheDefaultPostgresPortWhenTheUrlOmitsOne() {
    ConnectionTarget target = JdbcUrlParser.parse("jdbc:postgresql://localhost/mydb");

    assertEquals(JdbcUrlParser.DEFAULT_POSTGRES_PORT, target.port());
  }

  @Test
  void stripsTheQueryStringRatherThanTreatingItAsPartOfTheDatabaseName() {
    ConnectionTarget target =
        JdbcUrlParser.parse("jdbc:postgresql://localhost:5434/mydb?currentSchema=public");

    assertEquals("mydb", target.database());
  }

  @Test
  void worksOnADockerComposeServiceNameJustAsWellAsAHostname() {
    ConnectionTarget target = JdbcUrlParser.parse("jdbc:postgresql://postgres-db:5432/mydb");

    assertEquals("postgres-db", target.host());
    assertEquals(5432, target.port());
  }

  @Test
  void toleratesAUrlWithNoJdbcPrefixToo() {
    // DatabaseConfig's own javadoc notes nothing downstream actually speaks JDBC; this confirms
    // the parser does not depend on the prefix being present.
    ConnectionTarget target = JdbcUrlParser.parse("postgresql://localhost:5434/mydb");

    assertEquals("localhost", target.host());
    assertEquals(5434, target.port());
    assertEquals("mydb", target.database());
  }
}
