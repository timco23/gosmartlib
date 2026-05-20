package edu.ap.spring_backend.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                    "https://gsmartlib.tech",
                    "https://www.gsmartlib.tech",
                    "https://app.gsmartlib.tech"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders(
                    "Authorization",
                    "Content-Type",
                    "Accept",
                    "X-Requested-With"
                )
                .exposedHeaders("Authorization")
                .allowCredentials(true);
    }
}