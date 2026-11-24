package dev.johnmaluki.boardroom_booking_backend.config.security;

import lombok.Builder;

/** What a successful directory authentication tells us about the user. */
@Builder
public record LdapUserDetail(String username, String name, String email) {}
