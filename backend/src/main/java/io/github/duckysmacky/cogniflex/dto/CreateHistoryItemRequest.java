package io.github.duckysmacky.cogniflex.dto;

import io.github.duckysmacky.cogniflex.enums.DetectionKind;
import io.github.duckysmacky.cogniflex.enums.InputType;
import io.github.duckysmacky.cogniflex.enums.MediaType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateHistoryItemRequest(
        @NotNull
        InputType inputType,

        MediaType mediaType,

        @NotNull
        DetectionKind kind,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        double accuracy
) {
}
