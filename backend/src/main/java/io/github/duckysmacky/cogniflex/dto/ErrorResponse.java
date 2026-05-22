package io.github.duckysmacky.cogniflex.dto;

import java.time.LocalDateTime;

public record ErrorResponse(
    LocalDateTime timestamp,
    String error,
    String message
) {
    public ErrorResponse(String error, String message) {
        this(LocalDateTime.now(), error, message);
    }
}