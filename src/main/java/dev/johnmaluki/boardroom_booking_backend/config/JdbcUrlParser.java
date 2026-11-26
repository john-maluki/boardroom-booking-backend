package dev.johnmaluki.boardroom_booking_backend.config;

import java.net.URI;

/**
 * Pulls the host, port and database name out of the one {@code jdbc:postgresql://...} URL this
 * application treats as authoritative ({@code app.datasource.url}).
 *
 * <p>Exists so that every place that needs to connect to Postgres by some means other than R2DBC
 * itself — currently just {@code DatabaseBackupService}'s {@code pg_dump} — reads the exact same
 * target {@code DatabaseConfig} does, rather than maintaining its own separately-configured
 * host/port/database that can silently drift from what the running application actually talks
 * to. That drift already happened once in this project's own {@code .env}: a backup process
 * reading its own hardcoded host and a database name from a second, independent variable would
 * not have been dumping the database the app was actually using.
 */
public final class JdbcUrlParser {
  public static final int DEFAULT_POSTGRES_PORT = 5432;

  private JdbcUrlParser() {}

  /** Host, port and database name extracted from a {@code jdbc:postgresql://host:port/db} URL. */
  public record ConnectionTarget(String host, int port, String database) {}

  public static ConnectionTarget parse(String jdbcUrl) {
    URI uri = withoutJdbcSchemeOrQuery(jdbcUrl);
    String database = uri.getPath() == null ? "" : uri.getPath().replaceFirst("^/", "");
    int port = uri.getPort() == -1 ? DEFAULT_POSTGRES_PORT : uri.getPort();
    return new ConnectionTarget(uri.getHost(), port, database);
  }

  /**
   * {@code URI} cannot parse {@code jdbc:postgresql://host/db} directly, because the {@code
   * jdbc:} scheme leaves the rest opaque. Dropping that prefix — and any query string, which
   * {@code URI} would otherwise fold into the path it returns — gives a URI whose host, port and
   * path can be read normally.
   */
  private static URI withoutJdbcSchemeOrQuery(String jdbcUrl) {
    String withoutJdbc =
        jdbcUrl.startsWith("jdbc:") ? jdbcUrl.substring("jdbc:".length()) : jdbcUrl;
    int queryStart = withoutJdbc.indexOf('?');
    if (queryStart >= 0) {
      withoutJdbc = withoutJdbc.substring(0, queryStart);
    }
    return URI.create(withoutJdbc);
  }
}
