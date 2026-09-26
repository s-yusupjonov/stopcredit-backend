package uz.agrobank.stopcredit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "ldap")
public record LdapProperties(
        String url,
        String base,
        String managerDn,
        String managerPassword,
        String userSearchBase,
        String userSearchFilter) {
}