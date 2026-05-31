package io.github.duckysmacky.cogniflex.analysis.static_;

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
 *         incriminating than any one of them, so when at least {@link #BONUS_MIN_SIGNALS} distinct
 *         rules each raise MEDIUM-or-higher evidence the base score is multiplied by a capped
 *         factor.</li>
 * </ol>
 */
public final class StaticScoreCalculator {
    /** Returned when no rules ran at all (no information, rather than "not AI"). */
    public static final double NEUTRAL_AI_PROBABILITY = 0.5;

    /** Saturation rate of the noisy-OR squash; higher = a given raw score saturates faster. */
    private static final double SATURATION_RATE = 0.06;
    /** Minimum number of distinct MEDIUM+ rules before the combination bonus engages. */
    private static final int BONUS_MIN_SIGNALS = 3;
    /** Bonus added per distinct MEDIUM+ rule beyond the threshold. */
    private static final double BONUS_STEP = 0.15;
    /** Hard cap on the combination multiplier. */
    private static final double BONUS_MAX = 2.0;

    private StaticScoreCalculator() {
    }

    public static double calculate(List<RuleResult> results) {
        if (results.isEmpty()) {
            return NEUTRAL_AI_PROBABILITY;
        }

        double base = 1.0 - Math.exp(-SATURATION_RATE * rawScore(results));
        double factor = combinationFactor(results);
        return Math.min(1.0, base * factor);
    }

    private static double rawScore(List<RuleResult> results) {
        return results.stream()
            .filter(RuleResult::matched)
            .mapToDouble(result -> result.weight() * averageEvidenceScore(result.evidence()))
            .sum();
    }

    private static double averageEvidenceScore(List<Evidence> evidence) {
        if (evidence.isEmpty()) {
            return 1.0;
        }

        return evidence.stream()
            .mapToDouble(e -> e.confidence() * e.severity().multiplier())
            .average()
            .orElse(1.0);
    }

    private static double combinationFactor(List<RuleResult> results) {
        long distinctStrong = countDistinctStrongSignals(results);
        if (distinctStrong < BONUS_MIN_SIGNALS) {
            return 1.0;
        }

        double factor = 1.0 + BONUS_STEP * (distinctStrong - (BONUS_MIN_SIGNALS - 1));
        return Math.min(BONUS_MAX, factor);
    }

    private static long countDistinctStrongSignals(List<RuleResult> results) {
        return results.stream()
            .filter(RuleResult::matched)
            .filter(StaticScoreCalculator::hasStrongEvidence)
            .map(RuleResult::ruleCode)
            .distinct()
            .count();
    }

    private static boolean hasStrongEvidence(RuleResult result) {
        return result.evidence().stream()
            .anyMatch(e -> e.severity().atLeast(Evidence.Severity.MEDIUM));
    }
}
