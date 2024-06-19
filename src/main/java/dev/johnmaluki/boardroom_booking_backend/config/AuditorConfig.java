package dev.johnmaluki.boardroom_booking_backend.config;

import dev.johnmaluki.boardroom_booking_backend.core.security.AuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;

@Configuration
public class AuditorConfig {
    @Bean(name = "auditorProvider")
    public AuditorAware<Long> auditorProvider() {
        return new AuditorAwareImpl();
    }

}
