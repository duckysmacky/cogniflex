package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.RuleResult;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;

/**
 * Base class for text rules, removing the boilerplate of holding a {@code code}/{@code category}
 * and wrapping collected {@link Evidence} into a {@link RuleResult}.
 *
 * <p>Subclasses implement {@link #collectEvidence(TextAnalysisContext)} and return the hits they
 * found; an empty list becomes a {@link RuleResult#noMatch} automatically.
 */
public abstract class TextAnalysisRule extends AnalysisRule<TextAnalysisContext> {
    protected TextAnalysisRule(String code, Category category, StaticAnalysisConfig config) {
        super(code, category, config);
    }
}
