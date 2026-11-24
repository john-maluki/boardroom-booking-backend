package dev.johnmaluki.boardroom_booking_backend.user.view;

import dev.johnmaluki.boardroom_booking_backend.util.RoleType;

/** A user joined to the single role row that grants their authority. */
public record AppUserView(
    Long id,
    String username,
    String fullName,
    String department,
    String email,
    String timeZone,
    String tag,
    RoleType authority) {}
