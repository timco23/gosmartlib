package edu.ap.spring_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security config.
 * Uses stateless JWT-style auth (no server sessions, no form login).
 */
@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

   /**
     * Security rules:
     * - CSRF disabled (API usage)
     * - No sessions (STATELESS)
     * - /auth/**, /oauth/** and /api/** are public (for now)
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF for REST API
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**","/oauth/**" , "/api/**").permitAll()
                .anyRequest().permitAll() 
            )
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
            )
            .formLogin(form -> form.disable()) 
            .httpBasic(basic -> basic.disable()); 
        
        return http.build();
    }
}