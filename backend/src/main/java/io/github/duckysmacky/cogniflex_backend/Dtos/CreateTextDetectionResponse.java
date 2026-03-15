package io.github.duckysmacky.cogniflex_backend.Dtos;

import java.util.UUID;

public record CreateTextDetectionResponse(
        UUID id,
        String verdict,
        double confidence
) {}
