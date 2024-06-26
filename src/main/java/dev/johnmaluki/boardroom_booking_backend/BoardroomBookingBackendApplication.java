package dev.johnmaluki.boardroom_booking_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing(auditorAwareRef = "auditorProvider")
@SpringBootApplication
//@ComponentScan("com.unboundid")
public class BoardroomBookingBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(BoardroomBookingBackendApplication.class, args);
	}

}
