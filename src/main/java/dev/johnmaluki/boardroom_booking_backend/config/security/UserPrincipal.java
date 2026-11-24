package dev.johnmaluki.boardroom_booking_backend.config.security;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * The authenticated user as the rest of the application sees them.
 *
 * <p>The role now arrives as an explicit {@link RoleType} rather than being reached through a lazy
 * association: with R2DBC the role is a separate row, so whoever builds the principal loads it once
 * and passes it in. That keeps every authorisation check a field read instead of a query.
 */
@Setter
@Getter
public class UserPrincipal implements UserDetails {
  private String password;
  private final transient AppUser user;
  private final RoleType authority;

  public UserPrincipal(AppUser user, RoleType authority) {
    this.user = user;
    this.authority = authority == null ? RoleType.USER : authority;
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return List.of(new SimpleGrantedAuthority(this.authority.name()));
  }

  @Override
  public String getPassword() {
    return password;
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  public long getUserId() {
    return this.user.getId();
  }

  public String getUserTimeZone() {
    return this.user.getTimeZone();
  }

  public boolean isAdmin() {
    return this.authority == RoleType.ADMIN;
  }
}
