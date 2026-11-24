package dev.johnmaluki.boardroom_booking_backend;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Starting the context needs the three services in {@code docker-compose.yml} plus a {@code
 * .env}: the schema under {@code src/main/resources/tables/} is created automatically on
 * startup, the R2DBC pool opens its initial connections, and the RabbitMQ listener containers
 * start consuming. None of that can be stubbed out usefully, so the test is disabled by default
 * rather than left to fail on a machine without the stack running.
 *
 * <p>To run it: {@code docker compose up -d} and then remove the {@code @Disabled}.
 */
@SpringBootTest
@Disabled("Requires Postgres, Redis and RabbitMQ from docker-compose.yml plus a populated .env")
class BoardroomBookingBackendApplicationTests {

  @Test
  void contextLoads() {
    // Asserting nothing beyond the context coming up cleanly.
  }
}
