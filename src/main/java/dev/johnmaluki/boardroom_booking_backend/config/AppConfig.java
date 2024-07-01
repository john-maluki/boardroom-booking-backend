package dev.johnmaluki.boardroom_booking_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import net.datafaker.Faker;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
public class AppConfig {

    @Profile("dev")
    @Bean
    public Faker faker() {
        return new Faker();
    }
    @Bean
    public Dotenv dotenv() {
        return Dotenv.load();
    }

}
