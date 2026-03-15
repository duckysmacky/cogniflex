package io.github.duckysmacky.cogniflex_backend.Dtos;

import java.util.UUID;

public record CreateImageDetectionResponse(
        UUID id,
        String verdict,
        double confidence
) {
}