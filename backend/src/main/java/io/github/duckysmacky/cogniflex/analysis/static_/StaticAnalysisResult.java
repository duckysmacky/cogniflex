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
    private static final double NEUTRAL_AI_PROBABILITY = 0.5;

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
        return new StaticAnalysisResult(contentType, NEUTRAL_AI_PROBABILITY, null, null);
    }

    public static StaticAnalysisResult build(ContentItem item, List<RuleResult> ruleResults) {
        double aiProbability = calculateScore(ruleResults);

        List<Evidence> evidence = ruleResults.stream()
            .filter(RuleResult::matched)
            .flatMap(r -> r.evidence().stream())
            .toList();

        return new StaticAnalysisResult(item.contentType(), aiProbability, ruleResults, evidence);
    }

    private static double calculateScore(List<RuleResult> results) {
        double maxScore = results.stream()
            .mapToDouble(RuleResult::weight)
            .sum();

        if (maxScore == 0.0) return NEUTRAL_AI_PROBABILITY;

        double rawScore = results.stream()
            .filter(RuleResult::matched)
            .mapToDouble(r -> r.weight() * averageEvidenceScore(r.evidence()))
            .sum();

        return rawScore / maxScore;
    }

    private static double averageEvidenceScore(List<Evidence> evidence) {
        if (evidence.isEmpty()) return 1.0;

        return evidence.stream()
            .mapToDouble(e -> e.confidence() * e.severity().multiplier())
            .average()
            .orElse(1.0);
    }
}
