package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticScoringConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Turns the per-rule {@link RuleResult}s of a static analysis into a single AI probability in
 * {@code [0, 1]}.
 *
 * <p>The score is built in two stages:
 * <ol>
 *     <li><b>Weighted noisy-OR saturation.</b> Each matched rule contributes
 *         {@code weight · averageEvidence} to a raw total, which is squashed with
 *         {@code 1 - e^(-rate · raw)}. This is the closed form of a noisy-OR over the per-rule
 *         contributions: one strong rule already pushes the score high, independent signals
 *         accumulate toward 1, and the result needs no division by the total rule weight (so adding
 *         rules no longer dilutes existing scores).</li>
 *     <li><b>Combination bonus.</b> Co-occurrence of several independent tells is far more
 *         incriminating than any one of them, so when enough distinct rules each raise
 *         MEDIUM-or-higher evidence the base score is multiplied by a capped factor.</li>
 * </ol>
 */
@Component
public final class StaticScoreCalculator {
    /** Returned when no rules ran at all (no information, rather than "not AI"). */
    public static final double NEUTRAL_AI_PROBABILITY = 0.5;

    private final StaticScoringConfig config;

    @Autowired
    public StaticScoreCalculator(StaticAnalysisConfig config) {
        this(config.scoring());
    }

    public StaticScoreCalculator(StaticScoringConfig config) {
        this.config = config;
    }

    public double neutralAiProbability() {
        return NEUTRAL_AI_PROBABILITY;
    }

    public double calculate(List<RuleResult> results) {
        if (results.isEmpty()) {
            return NEUTRAL_AI_PROBABILITY;
        }

        double base = 1.0 - Math.exp(-config.saturationRate() * rawScore(results));
        double factor = combinationFactor(results);
        return Math.min(1.0, base * factor);
    }

    private double rawScore(List<RuleResult> results) {
        return results.stream()
            .filter(RuleResult::matched)
            .mapToDouble(result -> result.weight() * averageEvidenceScore(result.evidence()))
            .sum();
    }

    private double averageEvidenceScore(List<Evidence> evidence) {
        if (evidence.isEmpty()) {
            return 1.0;
        }

        return evidence.stream()
            .mapToDouble(e -> e.confidence() * e.severity().multiplier())
            .average()
            .orElse(1.0);
    }

    private double combinationFactor(List<RuleResult> results) {
        long distinctStrong = countDistinctStrongSignals(results);
        if (distinctStrong < config.bonusMinSignals()) {
            return 1.0;
        }

        double factor = 1.0 + config.bonusStep() * (distinctStrong - (config.bonusMinSignals() - 1));
        return Math.min(config.bonusMax(), factor);
    }

    private long countDistinctStrongSignals(List<RuleResult> results) {
        return results.stream()
            .filter(RuleResult::matched)
            .filter(this::hasStrongEvidence)
            .map(RuleResult::ruleCode)
            .distinct()
            .count();
    }

    private boolean hasStrongEvidence(RuleResult result) {
        return result.evidence().stream()
            .anyMatch(e -> e.severity().atLeast(Evidence.Severity.MEDIUM));
    }
}
