package dev.johnmaluki.boardroom_booking_backend.core.service;

import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class CurrentUserService {
  private UserPrincipal getCurrentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    return (UserPrincipal) authentication.getPrincipal();
  }

  public long getUserId() {
    return getCurrentUser().getUserId();
  }

  public RoleType getUserRole() {
    return this.getCurrentUser().getUser().getRole().getAuthority();
  }

  public AppUser getAppUser() {
    return this.getCurrentUser().getUser();
  }

  public String getAppUserTimezone() {
    return this.getCurrentUser().getUserTimeZone();
  }
}
