package dev.johnmaluki.boardroom_booking_backend.config.security.impl;

import dev.johnmaluki.boardroom_booking_backend.config.security.LdapService;
import dev.johnmaluki.boardroom_booking_backend.config.security.UserPrincipal;
import dev.johnmaluki.boardroom_booking_backend.user.model.AppUser;
import dev.johnmaluki.boardroom_booking_backend.user.model.Role;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppAdminRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.AppUserRepository;
import dev.johnmaluki.boardroom_booking_backend.user.repository.RoleRepository;
import dev.johnmaluki.boardroom_booking_backend.util.RoleType;
import dev.johnmaluki.boardroom_booking_backend.util.UserUtil;
import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;
import java.util.Optional;


@Service
@RequiredArgsConstructor
@Slf4j
public class LdapServiceImpl implements LdapService {
    private final Dotenv dotenv;
    private final AppUserRepository appUserRepository;
    private final AppAdminRepository appAdminRepository;
    private final RoleRepository roleRepository;
    private final UserUtil userUtil;


    @Override
    public UserPrincipal authenticateAndRetrieveUserInfo(String username, String password) {
        try {
            DirContext context = this.authenticateUserFromActiveDirectory(username, password);
            // Retrieve user information
           Map<String, String> userInfo = this.retrieveUserInformation(context, username);
           AppUser appUser = this.createUserOnDb(userInfo.get("username"), userInfo.get("email"), userInfo.get("fullName"));
            // Close the context after use
            context.close();
            return new UserPrincipal(appUser);
        } catch (NamingException e) {
            log.info("Problem authenticating user: {}", username);
            throw new UsernameNotFoundException("user not found");
        }
    }

    private DirContext authenticateUserFromActiveDirectory(String username, String password) throws NamingException {
        // Prepare the environment for creating the initial context
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
        env.put(Context.PROVIDER_URL, dotenv.get("LDAP_PROD_SERVER_URL_1"));
        env.put(Context.SECURITY_AUTHENTICATION, "simple");
        // Combine the username with the domain
        String principalName = username + "@" + dotenv.get("LDAP_PROD_DOMAIN_NAME");
        env.put(Context.SECURITY_PRINCIPAL, principalName);
        env.put(Context.SECURITY_CREDENTIALS, password);
        // Create initial context (this is the bind step)
        return new InitialDirContext(env);
    }

    private Map<String, String> retrieveUserInformation(DirContext context, String username) throws NamingException {
        // Set up the search controls
        SearchControls searchControls = new SearchControls();
        searchControls.setSearchScope(SearchControls.SUBTREE_SCOPE);
        // Search filter to find the user by their sAMAccountName (username)
        String searchFilter = "(sAMAccountName=" + username + ")";
        // Perform the search
        String baseSearchDn = "dc=kemri,dc=org";
        NamingEnumeration<SearchResult> results = context.search(baseSearchDn, searchFilter, searchControls);
        // Process the search results
        if (results.hasMore()) {
            SearchResult result = results.next();
            Attributes attributes = result.getAttributes();
            // Extract the 'displayName' attribute directly
            Attribute displayNameAttr = attributes.get("displayName");
            String displayName = displayNameAttr != null ? (String) displayNameAttr.get() : "N/A";
            // Extract the 'Email' attribute directly
            Attribute emailAttr = attributes.get("userPrincipalName");
            String email = emailAttr != null ? (String) emailAttr.get() : "N/A";
            var userInfo = new HashMap<String, String>();
            userInfo.put("username", username);
            userInfo.put("fullName", displayName);
            userInfo.put("email", email);
            return userInfo;
        } else {
            throw new UsernameNotFoundException("user not found");
        }
    }

    private AppUser createUserOnDb(String username, String email, String fullName) {
        Optional<AppUser> appUserOptional = appUserRepository.findByUsername(username);
        boolean isSystemAdmin = appAdminRepository.existsByEmail(email);
        if (appUserOptional.isPresent()) {
            AppUser appUser = appUserOptional.get();
            appUser.setUsername(username);
            appUser.setEmail(email);
            appUser.setFullName(fullName);
            Role role = appUser.getRole();
            if (isSystemAdmin) {
                role.setAuthority(RoleType.ADMIN);
            } else {
                role.setAuthority(RoleType.USER);
            }
            return appUserRepository.save(appUser);
        } else {
            Role role = Role.builder()
                    .authority(RoleType.USER)
                    .build();
            AppUser appUser = AppUser.builder()
                    .username(username)
                    .timeZone(userUtil.getDefaultUserTimeZone())
                    .role(role)
                    .email(email)
                    .fullName(fullName)
                    .build();
            role.setUser(appUser);
            if (isSystemAdmin) {
                appUser.getRole().setAuthority(RoleType.ADMIN);
            }
            return appUserRepository.save(appUser);
        }
    }

}
