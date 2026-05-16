package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.AnalysisContext;

public interface AnalysisRule<C extends AnalysisContext> {
    RuleResult evaluate(C context);
    RuleCategory getCategory();
    String getId();
}
