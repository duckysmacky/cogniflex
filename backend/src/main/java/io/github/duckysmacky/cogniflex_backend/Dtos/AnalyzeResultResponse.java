package io.github.duckysmacky.cogniflex_backend.Dtos;

import io.github.duckysmacky.cogniflex_backend.Enums.DetectionKind;

public record AnalyzeResultResponse(
        DetectionKind kind,
        double accuracy
) {
}
