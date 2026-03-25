package io.github.duckysmacky.cogniflex_backend.Dtos;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CreateHistoryRequest(
        @NotBlank
        @Pattern(regexp = "TEXT|IMAGE|VIDEO")
        String type,

        @Min(0)
        @Max(1)
        int kind,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        double accuracy
) {
}
