package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.AnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.ContentType;

public record DynamicAnalysisResult(
    ContentType contentType,
    AnalysisVerdict verdict,
    double confidence
) implements AnalysisResult {
    public DynamicAnalysisResult {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is required");
        }

        if (verdict == null) {
            throw new IllegalArgumentException("Dynamic verdict is required");
        }

        if (confidence < 0.0 || confidence > 1.0) {
            throw new IllegalArgumentException("Dynamic confidence must be between 0.0 and 1.0");
        }
    }

    @Override
    public double aiProbability() {
        return verdict == AnalysisVerdict.AI ? confidence : 1.0 - confidence;
    }
}
