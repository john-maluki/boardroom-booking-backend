package dev.johnmaluki.boardroom_booking_backend.core.security;

import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import jakarta.annotation.Nonnull;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    @Nonnull
    public Optional<Long> getCurrentAuditor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            return Optional.empty();
        }

        UserPrincipal userPrincipal = (UserPrincipal) authentication;
        return Optional.of(userPrincipal.getUserId());
    }
}
