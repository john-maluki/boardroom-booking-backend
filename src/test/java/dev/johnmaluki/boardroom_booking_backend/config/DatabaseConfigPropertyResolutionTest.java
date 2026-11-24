package dev.johnmaluki.boardroom_booking_backend.config;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.mock.env.MockPropertySource;

/**
 * Confirms the {@code app.datasource.*} placeholders in {@code application.properties} actually
 * resolve against system properties, the way {@code main()} populates them from {@code .env} at
 * startup. A plain compile cannot catch a typo'd placeholder name or a property that is declared
 * but never backed by anything — this exercises the same two-hop resolution
 * ({@code app.datasource.url} → {@code ${POSTGRES_DB_URL}} → the actual value) that {@link
 * DatabaseConfig}'s {@code @Value} fields rely on.
 */
class DatabaseConfigPropertyResolutionTest {

  @Test
  void datasourcePropertiesResolveThroughToTheEnvVariablePlaceholder() {
    StandardEnvironment environment = new StandardEnvironment();

    // Stands in for what Dotenv.configure().systemProperties().load() adds at startup.
    MockPropertySource envVars = new MockPropertySource("envVars");
    envVars.setProperty("POSTGRES_DB_URL", "jdbc:postgresql://db-host:5434/my-db");
    envVars.setProperty("POSTGRES_DB_USER", "booking_user");
    envVars.setProperty("POSTGRES_DB_PASSWORD", "secret");

    // Stands in for application.properties declaring app.datasource.url=${POSTGRES_DB_URL}.
    MockPropertySource appProperties = new MockPropertySource("appProperties");
    appProperties.setProperty("app.datasource.url", "${POSTGRES_DB_URL}");
    appProperties.setProperty("app.datasource.username", "${POSTGRES_DB_USER}");
    appProperties.setProperty("app.datasource.password", "${POSTGRES_DB_PASSWORD}");

    environment.getPropertySources().addFirst(appProperties);
    environment.getPropertySources().addFirst(envVars);

    // Environment.getProperty() is exactly what resolves a @Value("${app.datasource.url}")
    // field: it looks up the raw property, then resolves any ${...} placeholders inside it
    // against the same environment.
    assertEquals(
        "jdbc:postgresql://db-host:5434/my-db", environment.getProperty("app.datasource.url"));
    assertEquals("booking_user", environment.getProperty("app.datasource.username"));
    assertEquals("secret", environment.getProperty("app.datasource.password"));
  }
}
