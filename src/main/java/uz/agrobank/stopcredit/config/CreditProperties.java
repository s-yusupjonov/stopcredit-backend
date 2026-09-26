package uz.agrobank.stopcredit.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "credit")
public record CreditProperties(int reviewDeadlineDays) {
}
