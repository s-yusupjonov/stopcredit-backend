package uz.agrobank.stopcredit.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class LdapConfig {

    private final LdapProperties properties;

    public LdapConfig(LdapProperties properties) {
        this.properties = properties;
    }

    @Bean
    public LdapContextSource ldapContextSource() {

        LdapContextSource contextSource = new LdapContextSource();
        contextSource.setUrl(properties.url());
        contextSource.setBase(properties.base());
        contextSource.setUserDn(properties.managerDn());
        contextSource.setPassword(properties.managerPassword());
        contextSource.setReferral("ignore");

        Map<String, Object> baseEnv = new HashMap<>();
        baseEnv.put("java.naming.ldap.attributes.binary", "objectGUID objectSid");
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
}