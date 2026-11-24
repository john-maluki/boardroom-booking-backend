package dev.johnmaluki.boardroom_booking_backend;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * R2DBC auditing is enabled in {@code AuditorConfig} alongside the auditor bean it needs.
 *
 * <p>{@code @EnableAsync} is gone: the {@code @Async} email sends it supported have moved onto
 * RabbitMQ, so there is no longer a thread pool for the application to borrow.
 */
@EnableScheduling
@SpringBootApplication
public class BoardroomBookingBackendApplication {
  public static void main(String[] args) {
    // Pushed into JVM system properties, not just the Dotenv bean other classes read directly,
    // so that "${POSTGRES_DB_URL}"-style placeholders in the properties files resolve too. This
    // has to run before SpringApplication.run() — once the context starts binding @Value fields,
    // it is too late to add a property source. In production, where the real variables are
    // already set in the environment, .env is typically absent and this is a no-op: those
    // placeholders resolve from the OS environment instead, which Spring already reads.
    Dotenv.configure().ignoreIfMissing().systemProperties().load();
    SpringApplication.run(BoardroomBookingBackendApplication.class, args);
  }
}
