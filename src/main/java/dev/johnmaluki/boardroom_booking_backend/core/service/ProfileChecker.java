package dev.johnmaluki.boardroom_booking_backend.core.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class ProfileChecker {
    @Value("${spring.profiles.active}")
    private String activeProfiles;

    public boolean checkIfAppRunningLocally() {
        return activeProfiles.equals("dev");
    }
}
