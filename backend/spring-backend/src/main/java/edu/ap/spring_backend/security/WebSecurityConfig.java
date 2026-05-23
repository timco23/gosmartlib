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

    private static final String ROLE_ADMIN = "ADMIN";
    private static final String ROLE_LIBRARIAN = "BIBLIOTHEEKBEHEERDER";
    private static final String ROLE_TEACHER = "LEERKRACHT";
    private static final String ROLE_STUDENT = "LEERLING";

    private static final String[] ALL_USERS = {ROLE_STUDENT, ROLE_TEACHER, ROLE_LIBRARIAN, ROLE_ADMIN};
    private static final String[] TEACHER_PLUS = {ROLE_TEACHER, ROLE_LIBRARIAN, ROLE_ADMIN};
    private static final String[] LIBRARIAN_PLUS = {ROLE_LIBRARIAN, ROLE_ADMIN};

    private static final String PATH_BOOKS_ALL = "/books/**";
    private static final String PATH_ITEMS_ALL = "/items/**";
    private static final String PATH_TAGS_ALL = "/tags/**";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public WebSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors(Customizer.withDefaults())
            .csrf(csrf -> csrf.disable())
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

                //Restrict getting reviews by school and reports to librarians
                .requestMatchers(HttpMethod.GET,
                    "/reviews/schools/**",
                    "/reviews/*/reports"
                ).hasAnyAuthority(LIBRARIAN_PLUS)

                .requestMatchers(HttpMethod.GET,
                    PATH_BOOKS_ALL,
                    PATH_ITEMS_ALL,
                    "/favorites/**",
                    "/wishlist/**",
                    "/profile/**",
                    "/reviews/**",
                    "/genres/**",
                    "/subgenres/**",
                    "/reading-levels/**",
                    "/didactic-categories/**",
                    PATH_TAGS_ALL,
                    "/schools/search",
                    "/schools/search/**",
                    "/lendingHistory**",  
                    "/dashboard**",
                    "/lendings/last-borrowed/*",          
                    "/lendings/most-borrowed-this-month/*" 
                ).hasAnyAuthority(ALL_USERS)

                // ALL USERS: POST REVIEWS AND REPORTS
                .requestMatchers(HttpMethod.POST,
                    "/reviews/**"
                ).hasAnyAuthority(ALL_USERS)

                //Restrict deleting all reviews from a book to librarians
                .requestMatchers(HttpMethod.DELETE,
                    "/reviews/books/**"
                ).hasAnyAuthority(LIBRARIAN_PLUS)

               //Restrict approving a review to librarians
               .requestMatchers(HttpMethod.PUT,
                    "/reviews/*/approve"
               ).hasAnyAuthority(LIBRARIAN_PLUS)


                // ALL USERS
                .requestMatchers("/favorites/**", "/wishlist/**", "/profile/**", "/reviews/**")
                .hasAnyAuthority(ALL_USERS)

                .requestMatchers(HttpMethod.POST, "/user/request-deletion")
                .hasAnyAuthority(ALL_USERS)
                .requestMatchers(HttpMethod.DELETE, "/user/cancel-deletion")
                .hasAnyAuthority(ALL_USERS)

                //Reading list access for all users
                .requestMatchers(HttpMethod.GET, "/lists/{id}")
                .hasAnyAuthority(ALL_USERS)

                // TEACHER
                .requestMatchers("/lists/**")
                .hasAnyAuthority(TEACHER_PLUS)
                .requestMatchers("/statistics", "/statistics/**")
                .hasAnyAuthority(TEACHER_PLUS)
                .requestMatchers("/didactic-books")
                .hasAnyAuthority(TEACHER_PLUS)
                .requestMatchers(
                    "/books/*/report",
                    "/api/books/*/report"
                ).hasAnyAuthority(TEACHER_PLUS)
                .requestMatchers("/classes").hasAnyAuthority(TEACHER_PLUS)

                // BIBLIOTHEEKBEHEERDER
                .requestMatchers(HttpMethod.GET, "/lendings", "/lendings/**", "/collections", "/collections/**", "/book-lending-history/**", "/api/book-lending-history/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.GET, "/books/import/history")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.POST, "/lendings", "/lendings/**", "/collections", "/collections/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.PUT, "/lendings", "/lendings/**", "/collections", "/collections/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.DELETE, "/lendings", "/lendings/**", "/collections", "/collections/**")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.GET, "/schools/current", "/schools/users", "/user/school-users", "/user/profile/*")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.POST, PATH_BOOKS_ALL, PATH_ITEMS_ALL, PATH_TAGS_ALL)
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.PUT, PATH_BOOKS_ALL, PATH_ITEMS_ALL, PATH_TAGS_ALL, "/schools/current/settings", "/user/role")
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.PATCH, PATH_BOOKS_ALL, PATH_ITEMS_ALL, PATH_TAGS_ALL)
                .hasAnyAuthority(LIBRARIAN_PLUS)
                .requestMatchers(HttpMethod.DELETE, PATH_BOOKS_ALL, PATH_ITEMS_ALL, PATH_TAGS_ALL)
                .hasAnyAuthority(LIBRARIAN_PLUS)

                // ADMIN
                .requestMatchers("/schools/**", "/users/**").hasAuthority(ROLE_ADMIN)
                .requestMatchers(HttpMethod.PUT, "/user/school").hasAuthority(ROLE_ADMIN)
                .requestMatchers(HttpMethod.DELETE, "/user/delete").hasAuthority(ROLE_ADMIN) 

                .anyRequest().authenticated()
            )
            .formLogin(AbstractHttpConfigurer::disable)
            .httpBasic(AbstractHttpConfigurer::disable)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}