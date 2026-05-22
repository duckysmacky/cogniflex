package io.github.duckysmacky.cogniflex.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.rate-limit")
public record RateLimitProperties(
    List<String> pathPatterns,
    int capacity,
    int refillGreedyCapacity,
    long refillGreedyDurationSeconds,
    boolean active
) {
}
