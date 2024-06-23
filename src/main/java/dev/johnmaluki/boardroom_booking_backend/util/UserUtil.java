package dev.johnmaluki.boardroom_booking_backend.util;

import lombok.Getter;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
@Getter
public final class UserUtil {
    private final String defaultUserTimeZone;

    public UserUtil(Environment env) {
        this.defaultUserTimeZone = env.getProperty("user.default.timezone");
    }
}
