package dev.johnmaluki.boardroom_booking_backend.user.service;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Optional;

@Component
public class UserPrincipalDetailService implements UserDetailsService {

    private final LdapTemplate ldapTemplate;
    private final AppUserRepository userRepository;
    private final UserUtil userUtil;

    public UserPrincipalDetailService(LdapTemplate ldapTemplate, AppUserRepository userRepository, UserUtil userUtil) {
        this.ldapTemplate = ldapTemplate;
        this.userRepository = userRepository;
        this.userUtil = userUtil;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        EqualsFilter filter = new EqualsFilter("uid", username);
        List<LdapUserDetail> users = ldapTemplate.search("", filter.encode(), new UserAttributesMapper());
        if (users.isEmpty()){
            throw new UsernameNotFoundException("user not found");
        }
        LdapUserDetail ldapUserDetail = users.get(0);
        Optional<AppUser> dbUserOption = userRepository.findByUsername(username);
        if(dbUserOption.isPresent()){
            UserPrincipal userPrincipal = new UserPrincipal(dbUserOption.get());
            userPrincipal.setPassword(ldapUserDetail.getPassword());
            return userPrincipal;
        } else {
            AppUser user = AppUser.builder()
                    .username(ldapUserDetail.getUsername())
                    .email("email@test.com")
                    .timeZone(userUtil.getDefaultUserTimeZone())
                    .role(Role.builder()
                            .authority(RoleType.USER)
                            .build())
                    .build();
            user = userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            userPrincipal.setPassword(ldapUserDetail.getPassword());
            return userPrincipal;
        }

    }

    private static class UserAttributesMapper implements AttributesMapper<LdapUserDetail> {
        @Override
        public LdapUserDetail mapFromAttributes(Attributes attrs) throws NamingException {
            return LdapUserDetail.builder()
                    .username((String) attrs.get("uid").get())
                    .password(new String((byte[]) attrs.get("userPassword").get()))
                            .build();
        }


    }

    @Getter
    @Setter
    @Builder
    private static class LdapUserDetail {
        private String username;
        private String password;

    }
}
