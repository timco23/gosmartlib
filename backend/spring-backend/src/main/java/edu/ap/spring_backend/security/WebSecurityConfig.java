package edu.ap.spring_backend.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    private static final String[] ALL_USERS = {"LEERLING", "LEERKRACHT", "BIBLIOTHEEKBEHEERDER", "ADMIN"};
    private static final String[] TEACHER_PLUS = {"LEERKRACHT", "BIBLIOTHEEKBEHEERDER", "ADMIN"};
    private static final String[] LIBRARIAN_PLUS = {"BIBLIOTHEEKBEHEERDER", "ADMIN"};

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public auth endpoints
                .requestMatchers("/auth/login", "/auth/refresh", "/oauth/**", "/error").permitAll()
                .requestMatchers("/auth/me").authenticated()

                // Cover/image endpoints 
                .requestMatchers(HttpMethod.GET,
                    "/books/*/cover",
                    "/api/books/*/cover"
                ).permitAll()

                .requestMatchers(HttpMethod.GET,
                    "/collections/global/featured",
                    "/collections/school/*/featured",
                    "/api/collections/global/featured",
                    "/api/collections/school/*/featured",
                    "/lendings/user",
                    "/api/lendings/user"
                ).hasAnyAuthority(ALL_USERS)

                .requestMatchers(HttpMethod.GET,
                    "/books/**",
                    "/items/**",
                    "/favorites/**",
                    "/profile/**",
                    "/reviews/**",
                    "/genres/**",
                    "/subgenres/**",
                    "/reading-levels/**",
                    "/didactic-categories/**",
                    "/tags/**",
                    "/schools/search",
                    "/schools/search/**",
                    "/lendingHistory**",  
                    "/dashboard**"

                ).hasAnyAuthority(ALL_USERS)

                // ALL USERS: POST /books/{id}/reviews
                .requestMatchers(HttpMethod.POST,
                    "/books/*/reviews",
                    "/books/*/reviews/**",
                    "/api/books/*/reviews",
                    "/api/books/*/reviews/**"
                ).hasAnyAuthority(ALL_USERS)

                .requestMatchers(HttpMethod.DELETE,
                    "/books/*/reviews/*",
                    "/books/*/reviews/**",
                    "/api/books/*/reviews/*",
                    "/api/books/*/reviews/**"
                ).hasAnyAuthority(ALL_USERS)

                // ALL USERS
                .requestMatchers("/favorites/**", "/profile/**", "/reviews/**")
                .hasAnyAuthority(ALL_USERS)

                // TEACHER
                .requestMatchers("/reading-lists/**", "/lists/**")
                .hasAnyAuthority(TEACHER_PLUS)

                // BIBLIOTHEEKBEHEERDER
                .requestMatchers("/lendings/**", "/collections/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.GET, "/schools/current", "/user/school-users")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.POST, "/books/**", "/items/**", "/tags/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.PUT, "/books/**", "/items/**", "/tags/**", "/schools/current/settings")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.PATCH, "/books/**", "/items/**", "/tags/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.DELETE, "/books/**", "/items/**", "/tags/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)

                // ADMIN
                .requestMatchers("/schools/**", "/users/**").hasAuthority("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/user/school").hasAuthority("ADMIN")

                .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}