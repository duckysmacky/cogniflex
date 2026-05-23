package io.github.duckysmacky.cogniflex.dto;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;

public record AnalysisResultResponse(
    AnalysisVerdict verdict,
    double confidence
) {
}
