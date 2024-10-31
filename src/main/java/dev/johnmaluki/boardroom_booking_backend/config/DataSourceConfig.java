package dev.johnmaluki.boardroom_booking_backend.config;

import io.github.cdimascio.dotenv.Dotenv;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
public class DataSourceConfig {
  public final Dotenv dotenv;

  @Bean
  @Primary
  public DataSource dataSource() {
    String dbUrl = dotenv.get("POSTGRES_DB_URL");
    String dbUser = dotenv.get("POSTGRES_DB_USER");
    String dbPassword = dotenv.get("POSTGRES_DB_PASSWORD");
    return DataSourceBuilder.create()
        .url(dbUrl)
        .username(dbUser)
        .password(dbPassword)
        .build();
  }

}
