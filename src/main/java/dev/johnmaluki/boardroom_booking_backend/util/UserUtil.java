package dev.johnmaluki.boardroom_booking_backend.util;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
public final class UserUtil {
    private final Environment env;
    private final Dotenv dotenv;

    public List<String> getDefaultSystemAdministrators() {
        String defaultSystemAdminEmails = this.dotenv.get("DEFAULT_SYSTEM_ADMIN_EMAILS");
        return Arrays.asList(defaultSystemAdminEmails.split(","));
    }

    public String getDefaultUserTimeZone() {
        return env.getProperty("user.default.timezone");
    }
}
