package uz.agrobank.stopcredit.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import uz.agrobank.stopcredit.repository.UserRepository;
import uz.agrobank.stopcredit.security.JwtAuthenticationFilter;
import uz.agrobank.stopcredit.security.JwtService;
import uz.agrobank.stopcredit.security.ProblemResponseWriter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource(CorsProperties properties) {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(properties.allowedOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService,
                                                   UserRepository userRepository,
                                                   CorsConfigurationSource corsConfigurationSource,
                                                   ObjectMapper objectMapper) throws Exception {
        ProblemResponseWriter problems = new ProblemResponseWriter(objectMapper);
        http
                .cors(c -> c.configurationSource(corsConfigurationSource))
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(e -> e
                        .authenticationEntryPoint(problems.unauthorized())
                        .accessDeniedHandler(problems.forbidden()))
                .authorizeHttpRequests(auth -> auth
                        // error pages carry the original status; securing them again would mask it as 401
                        .dispatcherTypeMatchers(DispatcherType.ERROR).permitAll()
                        .requestMatchers("/error").permitAll()
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/api/users/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/cards/**", "/api/executors/**")
                        .hasAnyRole("ANTI_FRAUD", "MANAGEMENT")
                        .requestMatchers("/api/cards/**", "/api/executors/**").hasRole("ANTI_FRAUD")
                        .requestMatchers(HttpMethod.POST, "/api/credits").hasRole("ANTI_FRAUD")
                        // credit details are edited only by Anti-fraud while the credit is at its stage
                        .requestMatchers(HttpMethod.PUT, "/api/credits/*").hasRole("ANTI_FRAUD")
                        .requestMatchers(HttpMethod.PATCH, "/api/credits/*/status").hasRole("CREDIT_MANAGEMENT")
                        // stage ownership is enforced in CreditAccess
                        .requestMatchers("/api/credits/**").hasAnyRole(
                                "ANTI_FRAUD", "CREDIT_MANAGEMENT", "LEGAL", "UNDERWRITING", "MANAGEMENT")
                        .anyRequest().authenticated())
                .addFilterBefore(new JwtAuthenticationFilter(jwtService, userRepository),
                        UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}