package dev.johnmaluki.boardroom_booking_backend.core.security;

import jakarta.annotation.Nonnull;
import org.springframework.data.domain.AuditorAware;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    @Nonnull
    public Optional<Long> getCurrentAuditor() {
        return Optional.of(1L);
    }
}
