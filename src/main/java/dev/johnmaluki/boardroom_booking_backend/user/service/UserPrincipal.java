package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
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
        Optional<Role> role = Optional.ofNullable(user.getRole());
        if (role.isPresent()) {
            return List.of(new SimpleGrantedAuthority(user.getRole().getAuthority().name()));
        } else {
            return List.of();
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
}
