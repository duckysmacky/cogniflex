package io.github.duckysmacky.cogniflex.analysis.static_;

import java.util.List;

public record RuleResult(
    boolean matched,
    String ruleCode,
    double weight,
    List<Evidence> evidence
) {
    public static RuleResult match(String ruleCode, double weight, List<Evidence> evidence) {
        return new RuleResult(true, ruleCode, weight, evidence);
    }

    public static RuleResult noMatch(String ruleCode) {
        return new RuleResult(false, ruleCode, 0, List.of());
    }
}
