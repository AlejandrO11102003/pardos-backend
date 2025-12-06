package com.pardos.pos.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig {
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/api/**")
                    
                    .allowedOrigins("http://localhost:5173", "https://52a685a7bd4e.ngrok-free.app")
                    .allowedOriginPatterns("https://*.ngrok-free.app", "https://*.devtunnels.ms", "https://*.brs.devtunnels.ms", "https://*.loca.lt", "https://*.trycloudflare.com", "http://localhost:5173")
                    .allowedOriginPatterns("*")
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                    .allowedHeaders("*")
                    .allowCredentials(true);
            }
        };
    }
}
