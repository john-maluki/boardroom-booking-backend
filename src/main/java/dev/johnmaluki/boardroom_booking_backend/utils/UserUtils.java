package dev.johnmaluki.boardroom_booking_backend.utils;

import lombok.Getter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Getter
public final class UserUtils {
    private final String defaultUserTimeZone;

    public UserUtils(Environment env) {
        this.defaultUserTimeZone = env.getProperty("user.default.timezone");
    }
}
