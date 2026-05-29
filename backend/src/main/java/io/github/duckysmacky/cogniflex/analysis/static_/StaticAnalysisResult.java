package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;

import java.util.List;

public record StaticAnalysisResult(
    ContentType contentType,
    double aiProbability,
    List<Evidence> evidence
) {
    private static final double NEUTRAL_AI_PROBABILITY = 0.5;

    public StaticAnalysisResult {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is required");
        }
        if (aiProbability < 0.0 || aiProbability > 1.0) {
            throw new IllegalArgumentException("Static AI probability must be between 0.0 and 1.0");
        }
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }

    public static StaticAnalysisResult empty(ContentType contentType) {
        return new StaticAnalysisResult(contentType, NEUTRAL_AI_PROBABILITY, List.of());
    }

    public static StaticAnalysisResult build(ContentItem item, List<Evidence> evidence) {
        // TODO
        return new StaticAnalysisResult(item.contentType(), NEUTRAL_AI_PROBABILITY, evidence);
    }
}
