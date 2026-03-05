package edu.ap.spring_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for Spring Security.
 * Configures authentication, authorization, and password encoding.
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    /**
     * Configures HTTP security rules for REST API.
     * Disables CSRF, form login, and uses stateless sessions for SPA.
     * @param http HttpSecurity object to configure
     * @return configured SecurityFilterChain
     * @throws Exception if configuration fails
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/api/**").permitAll()
                .anyRequest().permitAll() 
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            )
            .formLogin(form -> form.disable()) 
            .httpBasic(basic -> basic.disable()); 
        
        return http.build();
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