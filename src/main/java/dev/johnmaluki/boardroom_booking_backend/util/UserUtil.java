package dev.johnmaluki.boardroom_booking_backend.util;

import io.github.cdimascio.dotenv.Dotenv;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
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

  public List<Long> getRecipientsFromString(String recipientString) {
    return Arrays.stream(recipientString.split(","))
        .map(String::trim) // Trim any spaces around values
        .map(
            val -> {
              try {
                return Long.parseLong(val);
              } catch (NumberFormatException e) {
                log.error("Cannot parse string value {} to Long. Invalid number", val);
                throw new IllegalArgumentException("Invalid number: " + val, e);
              }
            })
        .toList();
  }

  public String recipientsToString(List<Long> recipientList) {
    return recipientList.stream()
        .map(String::valueOf) // Convert each Long to a String
        .collect(Collectors.joining(",")); // Join with commas
  }
}
