package dev.johnmaluki.boardroom_booking_backend.config;

import dev.johnmaluki.boardroom_booking_backend.core.callback.BaseEntityCallback;
import dev.johnmaluki.boardroom_booking_backend.core.model.BaseEntity;
import dev.johnmaluki.boardroom_booking_backend.core.security.ReactiveAuditorAwareImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.ReactiveAuditorAware;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;

/**
 * Turns on R2DBC auditing and registers the callback that fills the UUID columns. Together these
 * replace {@code @EnableJpaAuditing} plus the entities' {@code @PrePersist} methods.
 */
@Configuration
@EnableR2dbcAuditing(auditorAwareRef = "auditorProvider")
public class AuditorConfig {

  @Bean(name = "auditorProvider")
  public ReactiveAuditorAware<Long> auditorProvider() {
    return new ReactiveAuditorAwareImpl();
  }

  @Bean
  public BeforeConvertCallback<BaseEntity> baseEntityCallback() {
    return new BaseEntityCallback();
  }
}
