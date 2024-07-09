package dev.johnmaluki.boardroom_booking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
public class BoardroomBookingBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BoardroomBookingBackendApplication.class, args);
	}
}
