package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * T20 - Cryptographic / distributional text watermark (stub).
 *
 * <p>Schemes such as SynthID-Text (a distributional watermark over token choices) or the
 * Aaronson green/red-list scheme require the official detector and the relevant key/config to
 * confirm. A positive detection would be near-definitive (CRITICAL, large weight), but until the
 * detector is wrapped this is a no-op slot carrying zero weight.
 */
@Component
public class TextWatermarkRule extends TextAnalysisRule {
    public TextWatermarkRule() {
        super("T20_TEXT_WATERMARK", Category.WATERMARK, 0.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        return List.of();
    }
}
