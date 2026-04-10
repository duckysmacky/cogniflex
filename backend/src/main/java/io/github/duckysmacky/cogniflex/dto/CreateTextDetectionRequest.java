package io.github.duckysmacky.cogniflex.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateTextDetectionRequest(
        @NotBlank
        String text
) {
}
