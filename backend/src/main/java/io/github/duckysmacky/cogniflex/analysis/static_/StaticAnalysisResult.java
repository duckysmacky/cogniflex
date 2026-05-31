package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.AnalysisResult;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;

import java.util.List;

public record StaticAnalysisResult(
    ContentType contentType,
    double aiProbability,
    List<RuleResult> ruleResults,
    List<Evidence> evidence
) implements AnalysisResult {
    public StaticAnalysisResult {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is required");
        }

        if (aiProbability < 0.0 || aiProbability > 1.0) {
            throw new IllegalArgumentException("Static AI probability must be between 0.0 and 1.0");
        }

        ruleResults = ruleResults == null ? List.of() : List.copyOf(ruleResults);
        evidence = evidence == null ? List.of() : List.copyOf(evidence);
    }

    public static StaticAnalysisResult empty(ContentType contentType) {
        return new StaticAnalysisResult(contentType, StaticScoreCalculator.NEUTRAL_AI_PROBABILITY, null, null);
    }

    public static StaticAnalysisResult build(
        ContentItem item,
        List<RuleResult> ruleResults,
        StaticScoreCalculator scoreCalculator
    ) {
        double aiProbability = scoreCalculator.calculate(ruleResults);

        List<Evidence> evidence = ruleResults.stream()
            .filter(RuleResult::matched)
            .flatMap(r -> r.evidence().stream())
            .toList();

        return new StaticAnalysisResult(item.contentType(), aiProbability, ruleResults, evidence);
    }
}
