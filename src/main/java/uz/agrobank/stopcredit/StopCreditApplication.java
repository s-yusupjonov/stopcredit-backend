package uz.agrobank.stopcredit;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.UserDetailsServiceAutoConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

// JWT is the only authentication; without the exclusion Boot adds an in-memory 'user' with a logged password
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class StopCreditApplication {

    public static void main(String[] args) {
        SpringApplication.run(StopCreditApplication.class, args);
    }
}
