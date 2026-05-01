package io.github.duckysmacky.cogniflex.dto;

import io.github.duckysmacky.cogniflex.enums.DetectionKind;

public record AnalyzeResultResponse(
        DetectionKind kind,
        double accuracy
) {
}
