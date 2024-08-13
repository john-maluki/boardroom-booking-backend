package dev.johnmaluki.boardroom_booking_backend.config.security;

public interface LdapService {
    UserPrincipal authenticateAndRetrieveUserInfo(String username, String password);
}
