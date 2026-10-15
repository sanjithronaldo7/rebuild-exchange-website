package com.rebuildexchange.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String uploadUri = uploadPath.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadUri.endsWith("/") ? uploadUri : uploadUri + "/");

        // Dynamically find project root to serve live frontend files
        Path current = Paths.get(".").toAbsolutePath().normalize();
        Path rootDir = java.nio.file.Files.exists(current.resolve("index.html")) ? current : current.getParent();
        String rootUri = rootDir != null ? rootDir.toUri().toString() : "";
        if (!rootUri.isEmpty() && !rootUri.endsWith("/")) {
            rootUri += "/";
        }

        registry.addResourceHandler("/**")
                .addResourceLocations(
                        rootUri,
                        "file:./src/main/resources/static/",
                        "classpath:/static/",
                        "classpath:/public/",
                        "classpath:/resources/",
                        "classpath:/META-INF/resources/"
                );
    }
}
