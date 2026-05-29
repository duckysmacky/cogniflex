package io.github.duckysmacky.cogniflex.analysis.static_;

public interface AnalysisRule<C extends AnalysisContext> {
    String code();
    Category category();
    RuleResult evaluate(C context);

    enum Category {
        METADATA,
        FILENAME,
        ENCODING
    }
}