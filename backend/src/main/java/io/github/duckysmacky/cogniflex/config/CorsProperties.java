package io.github.duckysmacky.cogniflex.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(
    List<String> allowedOriginsPatterns,
    List<String> allowedMethods,
    List<String> allowedHeaders,
    boolean allowCredentials
) {}