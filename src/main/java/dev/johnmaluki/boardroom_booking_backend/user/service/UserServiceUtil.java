package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;

import java.util.List;

public interface UserServiceUtil {
    /**
     * This method retrieves all system users who have ADMIN Role
     *
     * @return List of users with admin Role
     */
    List<AppUser> getAllSystemAdministrators();
}
