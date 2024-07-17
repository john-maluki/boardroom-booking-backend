package dev.johnmaluki.boardroom_booking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@EnableAsync
@SpringBootApplication
public class BoardroomBookingBackendApplication {
	public static void main(String[] args) {
		SpringApplication.run(BoardroomBookingBackendApplication.class, args);
	}
}
