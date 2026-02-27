package edu.ap.spring_backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Spring Security.
 * Configures authentication, authorization, and password encoding.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Configures HTTP security rules.
     * Disables CSRF and allows public access to authentication endpoints.
     * @param http HttpSecurity object to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**").permitAll()
                .anyRequest().permitAll()
            );
        return http.build();
    }
    
    /**
     * Creates an in-memory user details manager.
     * Allows adding new users without restarting the application.
     * @return InMemoryUserDetailsManager instance
     */
    @Bean
    public InMemoryUserDetailsManager inMemoryUserDetailsManager() {
        return new InMemoryUserDetailsManager(); 
    }

    /**
     * Creates a password encoder bean using BCrypt.
     * Ensures passwords are securely encrypted.
     * @return BCryptPasswordEncoder instance
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}