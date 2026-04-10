package io.github.duckysmacky.cogniflex.Dtos;

import jakarta.validation.constraints.NotBlank;

public record CreateTextDetectionRequest(
        @NotBlank
        String text
) {
}
