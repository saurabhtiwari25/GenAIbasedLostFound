package com.my.lostfound.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        // registry.addMapping("/**")
        //         .allowedOrigins("*")
        //         .allowedMethods("*")
        //         .allowedHeaders("*");
        
        registry.addMapping("/**")
                .allowedOrigins("https://gen-a-ibased-lost-found.vercel.app", "http://localhost:5173")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
