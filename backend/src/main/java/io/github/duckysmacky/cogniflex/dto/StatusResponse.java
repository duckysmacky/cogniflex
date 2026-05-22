package io.github.duckysmacky.cogniflex.dto;

import java.time.LocalDateTime;

public record StatusResponse(
    LocalDateTime timestamp,
    String backendHealth,
    String backendStatus,
    String MLServiceStatus,
    String databaseStatus,
    String redisStatus
) {
    public StatusResponse(
        String backendHealth,
        String backendStatus,
        String MLServiceStatus,
        String databaseStatus,
        String redisStatus
    ) {
        this(LocalDateTime.now(), backendHealth, backendStatus, MLServiceStatus, databaseStatus, redisStatus);
    }
}
