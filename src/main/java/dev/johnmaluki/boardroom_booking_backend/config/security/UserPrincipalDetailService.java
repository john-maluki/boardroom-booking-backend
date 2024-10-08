package dev.johnmaluki.boardroom_booking_backend.config.security;

import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.context.annotation.Profile;
import org.springframework.ldap.core.AttributesMapper;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.filter.EqualsFilter;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import java.util.List;
import java.util.Optional;

@Profile("dev")
@Component
@RequiredArgsConstructor
public class UserPrincipalDetailService implements AppUserDetailsService {

    private LdapTemplate ldapTemplate;
    private final AppUserRepository userRepository;
    private final UserUtil userUtil;


    @Override
    public UserPrincipal loadUserByUsername(String username) throws UsernameNotFoundException {
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
            Role role = Role.builder()
                    .authority(RoleType.USER)
                    .build();
            AppUser user = AppUser.builder()
                    .username(ldapUserDetail.getUsername())
                    .email("email@test.com")
                    .timeZone(userUtil.getDefaultUserTimeZone())
                    .role(role)
                    .build();
            role.setUser(user);
            user = userRepository.save(user);
            UserPrincipal userPrincipal = new UserPrincipal(user);
            userPrincipal.setPassword(ldapUserDetail.getPassword());
            return userPrincipal;
        }

    }

    @Override
    public void setLdapTemplate(LdapTemplate ldapTemplate) {
        this.ldapTemplate = ldapTemplate;
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
