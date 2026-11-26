package dev.johnmaluki.boardroom_booking_backend.config;

import dev.johnmaluki.boardroom_booking_backend.config.JdbcUrlParser.ConnectionTarget;
import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.spi.ConnectionFactories;
import io.r2dbc.spi.ConnectionFactory;
import io.r2dbc.spi.ConnectionFactoryOptions;
import java.io.IOException;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.r2dbc.connection.init.ConnectionFactoryInitializer;
import org.springframework.r2dbc.connection.init.ResourceDatabasePopulator;

/**
 * Database connectivity and schema setup.
 *
 * <p>There is no Flyway and no JPA, so there is no {@code ddl-auto} property to lean on either —
 * both are specific to other stacks this app no longer has. The R2DBC equivalent for "create the
 * tables on startup if they are not there yet" is {@link ConnectionFactoryInitializer}: it runs
 * the plain SQL files under {@code src/main/resources/tables/} against the database once, during
 * context startup. Every one of those files is written with {@code CREATE TABLE IF NOT EXISTS},
 * so running them again against a database that already has some or all of the tables is a no-op
 * for what exists and only creates what is missing.
 *
 * <p>Set {@code app.datasource.schema-init.enabled=false} to turn this off — for example against
 * a shared database you don't want the app altering, or if several instances start at once and
 * you would rather avoid them racing to create the same tables (Postgres's own {@code IF NOT
 * EXISTS} guards against the common case, but is not an airtight lock against two concurrent
 * creates). The SQL under {@code tables/} and its README stay the source of truth either way —
 * this just saves running them by hand on a fresh database.
 *
 * <p>{@code app.datasource.*} is declared in {@code application.properties} as {@code
 * ${POSTGRES_DB_URL}} etc. rather than read inline from {@code Dotenv} here, so the connection
 * target is configured the same way as every other Spring property and can be overridden per
 * profile without touching Java. The underlying {@code .env} values still have to reach Spring's
 * {@code Environment} for that placeholder to resolve — see the system-properties load in {@code
 * main()}.
 *
 * <p>{@code app.datasource.url} keeps the {@code jdbc:} prefix even though nothing here speaks
 * JDBC: it is still the one URL string the host/port/database are parsed out of, via {@link
 * JdbcUrlParser} — shared with {@code DatabaseBackupService}, so a {@code pg_dump} always targets
 * the exact same database this connection factory does.
 */
@Configuration
@Slf4j
public class DatabaseConfig {
  private static final String TABLE_SCRIPTS_LOCATION_PATTERN = "classpath:tables/*.sql";

  @Value("${app.datasource.url}")
  private String jdbcUrl;

  @Value("${app.datasource.username}")
  private String username;

  @Value("${app.datasource.password}")
  private String password;

  @Value("${app.datasource.schema-init.enabled:true}")
  private boolean schemaInitEnabled;

  @Value("${spring.r2dbc.pool.initial-size:5}")
  private int poolInitialSize;

  @Value("${spring.r2dbc.pool.max-size:20}")
  private int poolMaxSize;

  @Value("${spring.r2dbc.pool.max-idle-time:10m}")
  private Duration poolMaxIdleTime;

  /**
   * Runs {@code tables/*.sql} once at startup, in filename order (the same order the README
   * under {@code tables/} documents for running them by hand), against a plain, single-use
   * connection factory — the pool below is for the application's own queries, not for a one-off
   * script run before the application is otherwise up.
   */
  @Bean
  public ConnectionFactoryInitializer connectionFactoryInitializer() {
    ConnectionFactoryInitializer initializer = new ConnectionFactoryInitializer();
    // Unpooled and used once: this is a one-off script run, not application traffic, so it has
    // no reason to borrow from (or hold open) a slot in the pool below.
    initializer.setConnectionFactory(rawConnectionFactory());
    initializer.setDatabasePopulator(new ResourceDatabasePopulator(tableScripts()));
    initializer.setEnabled(schemaInitEnabled);
    return initializer;
  }

  /**
   * The pooled connection factory every application query runs through.
   *
   * <p>Pooling has to be wired explicitly. Spring Boot only applies {@code spring.r2dbc.pool.*}
   * to a connection factory it builds itself, and because the URL comes from {@code
   * app.datasource.url} rather than {@code spring.r2dbc.url} this one is built here — so Boot's
   * auto-configuration backs off and takes its pooling with it. Without the wrapper below every
   * query would open and close its own connection, which on a reactive stack means the handful of
   * event loops spend their time waiting on TCP and TLS handshakes.
   *
   * <p>The {@code spring.r2dbc.pool.*} properties are read directly for the same reason, so that
   * what the properties file says is what the pool actually does.
   *
   * <p>Taking {@link ConnectionFactoryInitializer} as a parameter is what guarantees the table
   * scripts have finished running — {@code InitializingBean.afterPropertiesSet()} runs
   * synchronously during that bean's own creation — before the first pooled connection is handed
   * out.
   */
  @Bean(destroyMethod = "dispose")
  @Primary
  public ConnectionFactory connectionFactory(
      ConnectionFactoryInitializer connectionFactoryInitializer) {
    ConnectionFactory postgres = rawConnectionFactory();
    return new ConnectionPool(
        ConnectionPoolConfiguration.builder(postgres)
            .name("boardroom-r2dbc-pool")
            .initialSize(poolInitialSize)
            .maxSize(poolMaxSize)
            .maxIdleTime(poolMaxIdleTime)
            // A borrow that cannot be satisfied fails rather than hanging the request forever.
            .maxAcquireTime(Duration.ofSeconds(30))
            .validationQuery("SELECT 1")
            .build());
  }

  private ConnectionFactory rawConnectionFactory() {
    ConnectionTarget target = JdbcUrlParser.parse(jdbcUrl);
    log.info(
        "Connecting to Postgres over R2DBC at {}:{}/{}",
        target.host(),
        target.port(),
        target.database());

    return ConnectionFactories.get(
        ConnectionFactoryOptions.builder()
            .option(ConnectionFactoryOptions.DRIVER, "postgresql")
            .option(ConnectionFactoryOptions.HOST, target.host())
            .option(ConnectionFactoryOptions.PORT, target.port())
            .option(ConnectionFactoryOptions.DATABASE, target.database())
            .option(ConnectionFactoryOptions.USER, username)
            .option(ConnectionFactoryOptions.PASSWORD, password)
            .build());
  }

  /**
   * {@code tables/*.sql}, sorted by filename. The resource pattern resolver does not guarantee an
   * order on its own, and the files are numbered ({@code 01_users.sql}, {@code
   * 02_administrators.sql}, ...) precisely so foreign-key-dependent tables are created after the
   * tables they reference — sorting restores that order explicitly rather than relying on
   * whatever order the classpath scan happens to return.
   */
  private Resource[] tableScripts() {
    try {
      Resource[] resources =
          new PathMatchingResourcePatternResolver().getResources(TABLE_SCRIPTS_LOCATION_PATTERN);
      Arrays.sort(resources, Comparator.comparing(Resource::getFilename));
      return resources;
    } catch (IOException e) {
      throw new IllegalStateException(
          "Could not list table DDL scripts at " + TABLE_SCRIPTS_LOCATION_PATTERN, e);
    }
  }
}
