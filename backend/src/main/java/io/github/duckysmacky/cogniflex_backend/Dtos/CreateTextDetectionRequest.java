package io.github.duckysmacky.cogniflex_backend.Dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateTextDetectionRequest(
        @NotBlank
        String text
) {
}
