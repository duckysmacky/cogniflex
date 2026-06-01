package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticScoringConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticScoreCalculatorTest {
    private final StaticScoreCalculator calculator = new StaticScoreCalculator(
        new StaticScoringConfig(10.0, 3, 0.08, 2.0)
    );

    @Test
    void emptyResultsAreNeutral() {
        assertEquals(StaticScoreCalculator.NEUTRAL_AI_PROBABILITY, calculator.calculate(List.of()));
    }

    @Test
    void noMatchesScoreZero() {
        double score = calculator.calculate(List.of(
            RuleResult.noMatch("a", 10.0),
            RuleResult.noMatch("b", 20.0)
        ));

        assertEquals(0.0, score, 1e-9);
    }

    @Test
    void singleCriticalSignalDominates() {
        double score = calculator.calculate(List.of(
            matched("T1", 60.0, Evidence.Severity.CRITICAL, 1.0)
        ));

        assertTrue(score > 0.9, "expected a strong CRITICAL hit to dominate, got " + score);
    }

    @Test
    void singleWeakSignalStaysLow() {
        // Include realistic unmatched rules so totalActiveWeight reflects the full 18-rule context.
        // Without dilution by unmatched weight, a single rule's normalizedRaw would be 0.25
        // (evidence score only) and produce an inflated base.
        List<RuleResult> results = new java.util.ArrayList<>(List.of(
            matched("T8", 10.0, Evidence.Severity.MEDIUM, 0.5)
        ));
        for (int i = 0; i < 17; i++) {
            results.add(RuleResult.noMatch("FILLER_" + i, 15.0));
        }

        double score = calculator.calculate(results);

        assertTrue(score < 0.25, "expected a lone weak signal to stay low, got " + score);
    }

    @Test
    void threeDistinctMediumSignalsGetTheBonus() {
        // Both scenarios share the same raw score (and therefore the same base), so any
        // difference is purely the combination bonus.
        double withBonus = calculator.calculate(List.of(
            matched("a", 10.0, Evidence.Severity.MEDIUM, 0.5),
            matched("b", 10.0, Evidence.Severity.MEDIUM, 0.5),
            matched("c", 10.0, Evidence.Severity.MEDIUM, 0.5)
        ));
        double withoutBonus = calculator.calculate(List.of(
            matched("single", 30.0, Evidence.Severity.MEDIUM, 0.5)
        ));

        assertTrue(withBonus > withoutBonus, "three distinct signals should outscore one");
    }

    @Test
    void twoDistinctSignalsGetNoBonus() {
        double two = calculator.calculate(List.of(
            matched("a", 15.0, Evidence.Severity.MEDIUM, 0.5),
            matched("b", 15.0, Evidence.Severity.MEDIUM, 0.5)
        ));
        double one = calculator.calculate(List.of(
            matched("single", 30.0, Evidence.Severity.MEDIUM, 0.5)
        ));

        assertEquals(one, two, 1e-9);
    }

    @Test
    void lowSeverityDoesNotCountTowardBonus() {
        double threeLow = calculator.calculate(List.of(
            matched("a", 10.0, Evidence.Severity.LOW, 0.5),
            matched("b", 10.0, Evidence.Severity.LOW, 0.5),
            matched("c", 10.0, Evidence.Severity.LOW, 0.5)
        ));
        double one = calculator.calculate(List.of(
            matched("single", 30.0, Evidence.Severity.LOW, 0.5)
        ));

        assertEquals(one, threeLow, 1e-9);
    }

    private static RuleResult matched(String code, double weight, Evidence.Severity severity, double confidence) {
        Evidence evidence = Evidence.builder()
            .ruleCode(code)
            .severity(severity)
            .confidence(confidence)
            .matchedValue("x")
            .explanation("x")
            .build();
        return RuleResult.match(code, weight, List.of(evidence));
    }
}
