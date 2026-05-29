package io.github.duckysmacky.cogniflex.analysis.static_;

import java.util.List;

public record RuleResult(
    String ruleCode,
    boolean matched,
    List<Evidence> evidence
) {
}
