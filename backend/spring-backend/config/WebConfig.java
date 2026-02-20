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
                    "http://localhost",
                    "http://192.168.*",
                    "http://app.195.201.32.74.traefik.me",
                    "https://app.195.201.32.74.traefik.me",
                    "http://gsmartlib.tech",
                    "http://www.gsmartlib.tech",
                    "http://app.gsmartlib.tech",
                    "https://gsmartlib.tech",
                    "https://www.gsmartlib.tech",
                    "https://app.gsmartlib.tech"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}