package dev.johnmaluki.boardroom_booking_backend.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.ldap.LdapUtils;
import org.springframework.stereotype.Service;

import javax.naming.directory.DirContext;
import javax.naming.ldap.LdapContext;

@Service
@RequiredArgsConstructor
public class LdapService {
    private final LdapTemplate ldapTemplate;
    private final LdapContextSource ldapContextSource;

    public boolean authenticateUserFromActiveDirectory(String username, String password) {
//        String userDn = "objectClass=user,sAMAccountName=" + username + ",ou=users,dc=kemri,dc=org";
        String userDn = String.format("sAMAccountName=%s,og=users,dc=kemri,dc=org", username);
        try {
            LdapContextSource userContextSource = new LdapContextSource();
            userContextSource.setUrl("ldap://10.0.2.53");
            userContextSource.setBase("dc=kemri,dc=org");
            userContextSource.setUserDn(username);
            userContextSource.setPassword(password);
            userContextSource.afterPropertiesSet();

            LdapTemplate userLdapTemplate = new LdapTemplate(userContextSource);
            userLdapTemplate.authenticate("", "sAMAccountName=" + username + ")", password);
            return true;
        } catch (Exception e) {
            System.out.println(e.getMessage());
            return false;
        }
    }

}
