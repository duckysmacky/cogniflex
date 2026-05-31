package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.RuleResult;

import java.util.List;

/**
 * Base class for text rules, removing the boilerplate of holding a {@code code}/{@code category}/
 * {@code weight} and wrapping collected {@link Evidence} into a {@link RuleResult}.
 *
 * <p>Subclasses implement {@link #collectEvidence(TextAnalysisContext)} and return the hits they
 * found; an empty list becomes a {@link RuleResult#noMatch} automatically.
 */
public abstract class TextAnalysisRule implements AnalysisRule<TextAnalysisContext> {
    private final String code;
    private final Category category;
    private final double weight;

    protected TextAnalysisRule(String code, Category category, double weight) {
        this.code = code;
        this.category = category;
        this.weight = weight;
    }

    @Override
    public final String code() {
        return code;
    }

    @Override
    public final Category category() {
        return category;
    }

    @Override
    public final double weight() {
        return weight;
    }

    @Override
    public final RuleResult evaluate(TextAnalysisContext context) {
        List<Evidence> evidence = collectEvidence(context);

        if (evidence == null || evidence.isEmpty()) {
            return RuleResult.noMatch(code, weight);
        }

        return RuleResult.match(code, weight, List.copyOf(evidence));
    }

    /**
     * Inspects the context and returns one {@link Evidence} per concrete hit, or an empty list
     * when the signal is absent.
     */
    protected abstract List<Evidence> collectEvidence(TextAnalysisContext context);

    /**
     * Convenience for building an {@link Evidence} already tagged with this rule's code.
     */
    protected Evidence.Builder evidence() {
        return Evidence.builder()
            .ruleCode(code);
    }
}
