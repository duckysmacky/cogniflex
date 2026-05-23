package io.github.duckysmacky.cogniflex.dto;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.InputType;
import io.github.duckysmacky.cogniflex.analysis.MediaType;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record CreateHistoryItemRequest(
        @NotNull
        InputType inputType,

        MediaType mediaType,

        @NotNull
        AnalysisVerdict verdict,

        @DecimalMin("0.0")
        @DecimalMax("1.0")
        double accuracy
) {
}
