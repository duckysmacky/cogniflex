package io.github.duckysmacky.cogniflex.Dtos;

import io.github.duckysmacky.cogniflex.Enums.DetectionKind;

public record AnalyzeResultResponse(
        DetectionKind kind,
        double accuracy
) {
}
