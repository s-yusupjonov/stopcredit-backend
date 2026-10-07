package uz.agrobank.stopcredit.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapEncoder;
import org.springframework.stereotype.Service;
import uz.agrobank.stopcredit.config.LdapProperties;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class LdapAuthenticationService {

    private static final String DISPLAY_NAME_ATTR = "displayName";

    private final LdapTemplate ldapTemplate;
    private final LdapProperties properties;

    public boolean authenticate(String username, String password) {

        String filter = resolveFilter(username);

        try {
            boolean result = ldapTemplate.authenticate(properties.userSearchBase(), filter, password);
            log.info("ldap-auth: username={}, base={}, result={}", username, properties.userSearchBase(), result);
            return result;
        } catch (RuntimeException e) {
            log.warn("ldap-auth-failed: username={}, base={}, exceptionClass={}, message={}",
                    username, properties.userSearchBase(), e.getClass().getName(), e.getMessage());
            return false;
        }
    }

    public Optional<String> lookupFullName(String username) {

        String filter = resolveFilter(username);

        try {
            List<String> displayNames = ldapTemplate.search(properties.userSearchBase(), filter, this::displayNameOf);
            return displayNames.stream().findFirst().map(name -> name.isBlank() ? username : name);
        } catch (RuntimeException e) {
            log.warn("ldap-full-name-lookup-failed: username={}, base={}, message={}",
                    username, properties.userSearchBase(), e.getMessage());
            return Optional.empty();
        }
    }

    private String resolveFilter(String username) {
        return MessageFormat.format(properties.userSearchFilter(), LdapEncoder.filterEncode(username));
    }

    private String displayNameOf(Attributes attrs) throws javax.naming.NamingException {
        Attribute attr = attrs.get(DISPLAY_NAME_ATTR);
        return attr != null ? String.valueOf(attr.get()) : "";
    }
}