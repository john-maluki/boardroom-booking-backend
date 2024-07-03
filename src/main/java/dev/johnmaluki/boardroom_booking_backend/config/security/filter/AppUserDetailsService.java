package dev.johnmaluki.boardroom_booking_backend.config.security.filter;

import org.springframework.ldap.core.LdapTemplate;
import org.springframework.security.core.userdetails.UserDetailsService;

public interface AppUserDetailsService extends UserDetailsService {
    void setLdapTemplate(LdapTemplate ldapTemplate);
}
