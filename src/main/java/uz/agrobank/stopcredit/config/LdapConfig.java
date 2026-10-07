package uz.agrobank.stopcredit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Configuration
public class LdapConfig {

    private static final String CONNECT_TIMEOUT_MS = "5000";
    private static final String READ_TIMEOUT_MS = "10000";

    private final LdapProperties properties;

    public LdapConfig(LdapProperties properties) {
        this.properties = properties;
    }

    @Bean
    public LdapContextSource ldapContextSource() {
        warnAboutUnsafeSettings();

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(properties.url());
        contextSource.setBase(properties.base());
        contextSource.setUserDn(Objects.requireNonNullElse(properties.managerDn(), ""));
        contextSource.setPassword(Objects.requireNonNullElse(properties.managerPassword(), ""));
        contextSource.setReferral("ignore");

        Map<String, Object> baseEnv = new HashMap<>();
        baseEnv.put("java.naming.ldap.attributes.binary", "objectGUID objectSid");
        baseEnv.put("com.sun.jndi.ldap.connect.timeout", CONNECT_TIMEOUT_MS);
        baseEnv.put("com.sun.jndi.ldap.read.timeout", READ_TIMEOUT_MS);
        contextSource.setBaseEnvironmentProperties(baseEnv);

        contextSource.afterPropertiesSet();

        return contextSource;
    }

    @Bean
    public LdapTemplate ldapTemplate(LdapContextSource ldapContextSource) {

        LdapTemplate ldapTemplate = new LdapTemplate(ldapContextSource);
        ldapTemplate.setIgnorePartialResultException(true);

        return ldapTemplate;
    }

    private void warnAboutUnsafeSettings() {
        if (!StringUtils.hasText(properties.managerDn()) || !StringUtils.hasText(properties.managerPassword())) {
            log.warn("LDAP_MANAGER_DN / LDAP_MANAGER_PASSWORD are not set - Active Directory logins will fail");
        }
        if (properties.url() != null && properties.url().toLowerCase(Locale.ROOT).startsWith("ldap://")) {
            log.warn("LDAP_URL uses plain ldap:// - bind passwords travel unencrypted; use ldaps:// in production");
        }
    }
}
