package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticRuleConfig;

import java.util.List;

public abstract class AnalysisRule<C extends AnalysisContext> {
    private final String code;
    private final Category category;
    private final boolean enabled;
    private final double weight;

    protected AnalysisRule(String code, Category category, StaticAnalysisConfig config) {
        this.code = code;
        this.category = category;

        StaticRuleConfig ruleConfig = config.rule(code);
        this.enabled = ruleConfig.enabled();
        this.weight = ruleConfig.weight();
    }

    public final String code() {
        return code;
    }

    public final Category category() {
        return category;
    }

    public final boolean enabled() {
        return enabled;
    }

    public final double weight() {
        return weight;
    }

    public final RuleResult evaluate(C context) {
        List<Evidence> evidence = collectEvidence(context);

        if (evidence == null || evidence.isEmpty()) {
            return RuleResult.noMatch(code, weight);
        }

        return RuleResult.match(code, weight, List.copyOf(evidence));
    }

    protected abstract List<Evidence> collectEvidence(C context);

    protected Evidence.Builder evidence() {
        return Evidence.builder()
            .ruleCode(code);
    }

    public enum Category {
        METADATA,
        FILENAME,
        ENCODING,
        LEAKAGE,
        HIDDEN_CHARACTERS,
        TYPOGRAPHY,
        LEXICAL,
        PHRASING,
        STRUCTURE,
        STYLOMETRY,
        SEMANTIC,
        WATERMARK
    }
}
