package dev.johnmaluki.boardroom_booking_backend.util;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

@Component
public class DateTimeUtil {
    private ZonedDateTime convertISOStringToUTCZonedDateTime(String isoDateString) {
        return ZonedDateTime.parse(isoDateString);
    }

    /**
     * The object returned is in UTC
     * @param isoDateString
     * @return returns LocalDateTime object that is on UTC zone id
     */
    public LocalDateTime obtainLocalDateTimeFromISOString(String isoDateString) {
        return this.convertISOStringToUTCZonedDateTime(isoDateString).toLocalDateTime();
    }

    public LocalDateTime obtainLocalDateTimeBasedOnUserZone(LocalDateTime localDateTime, String userTimezone) {
        ZoneId userZoneId = ZoneId.of(userTimezone);
        ZoneId utcZoneId = ZoneId.of("UTC");
        ZonedDateTime utcZonedDateTime = localDateTime.atZone(utcZoneId);
        ZonedDateTime userZonedDateTime = utcZonedDateTime.withZoneSameInstant(userZoneId);
        return userZonedDateTime.toLocalDateTime();
    }

    public LocalDateTime getCurrentLocalDateTimeUtc() {
        return LocalDateTime.now(ZoneId.of("UTC"));
    }
}
