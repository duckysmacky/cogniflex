package io.github.duckysmacky.cogniflex.dto;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;

import java.util.List;

public record AnalysisResultResponse(
    AnalysisVerdict verdict,
    double confidence,
    List<EvidenceResponse> evidence
) {
    public AnalysisResultResponse {
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }

    public AnalysisResultResponse(AnalysisVerdict verdict, double confidence) {
        this(verdict, confidence, List.of());
    }
}
