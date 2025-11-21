package com.freelancer.portal.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Web MVC configuration for serving static resources including uploaded files.
 */
@Configuration
@Slf4j
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Resolve the absolute path for uploads directory
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadPathString = uploadPath.toUri().toString();
        
        log.info("Configuring resource handler for uploads at: {}", uploadPathString);
        
        // Serve uploaded files (including profile pictures)
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadPathString);
        
        log.info("Resource handler registered: /uploads/** -> {}", uploadPathString);
    }
}
