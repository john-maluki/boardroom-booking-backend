package dev.johnmaluki.boardroom_booking_backend.config.security;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Setter
@Getter
public class UserPrincipal implements UserDetails {
    private String password;
    private final transient AppUser user;

    public UserPrincipal(AppUser user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Optional<Role> roleOptional = Optional.ofNullable(user.getRole());
        if (roleOptional.isPresent()) {
            RoleType roleType = roleOptional.get().getAuthority();
            return List.of(new SimpleGrantedAuthority(roleType.name()));
        } else {
            return Collections.emptyList();
        }

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

}
