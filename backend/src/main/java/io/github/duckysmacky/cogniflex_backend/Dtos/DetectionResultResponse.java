package io.github.duckysmacky.cogniflex_backend.Dtos;

import java.util.UUID;

public record DetectionResultResponse(
        UUID id,
        String inputType,
        String verdict,
        double confidence,
        String createdAt
) {
}