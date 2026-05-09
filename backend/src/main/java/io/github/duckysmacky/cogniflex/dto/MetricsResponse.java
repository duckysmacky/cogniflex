package io.github.duckysmacky.cogniflex.dto;

public record MetricsResponse(
    String databaseCallbackTime,
    String redisCallbackTime,
    String modelCallbackTime
) {
}
