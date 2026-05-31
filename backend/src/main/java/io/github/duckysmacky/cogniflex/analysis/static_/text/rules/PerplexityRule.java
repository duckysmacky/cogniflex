package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * T16 - Low perplexity / high predictability (stub).
 *
 * <p>AI text is "average", so each token is highly predictable. Estimating this without the source
 * model requires shipping a small reference n-gram language model and a scorer - the most expensive
 * "static" text rule. Left as a no-op slot until that model is integrated; it deliberately carries
 * zero weight so it does not affect the aggregate score.
 */
@Component
public class PerplexityRule extends TextAnalysisRule {
    public PerplexityRule() {
        super("T16_PERPLEXITY", Category.STYLOMETRY, 0.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        return List.of();
    }
}
