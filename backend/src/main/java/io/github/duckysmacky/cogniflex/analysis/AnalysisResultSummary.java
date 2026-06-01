package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;

import java.util.List;

public record AnalysisResultSummary(
    FinalScore score,
    List<Evidence> evidence
) {
    public AnalysisResultSummary {
        if (score == null) {
            throw new IllegalArgumentException("Final score is required");
        }

        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }
}
