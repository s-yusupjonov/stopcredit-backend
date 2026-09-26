package uz.agrobank.stopcredit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "admin")
public record AdminProperties(String defaultUsername, String defaultPassword) {
}
