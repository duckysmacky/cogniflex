package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * T15 - Low sentence-length variance (burstiness).
 *
 * <p>Human writing is "bursty" - short sentences sit next to long ones - whereas AI text tends
 * toward uniform sentence length. The rule computes the coefficient of variation
 * ({@code stddev / mean}) of per-sentence word counts; a low value flags uniformity. Confidence is
 * moderate and the rule only fires on a sufficiently long sample (short texts are unreliable).
 */
@Component
public class SentenceLengthVarianceRule extends TextAnalysisRule {
    private static final int MIN_SENTENCES = 8;
    private static final double MEDIUM_CV = 0.25;
    private static final double LOW_CV = 0.4;

    public SentenceLengthVarianceRule() {
        super("T15_SENTENCE_LENGTH_VARIANCE", Category.STYLOMETRY, 12.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        List<Integer> lengths = context.sentenceWordCounts();
        if (lengths.size() < MIN_SENTENCES) {
            return List.of();
        }

        double mean = lengths.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);

        if (mean <= 0.0) {
            return List.of();
        }

        double variance = lengths.stream()
            .mapToDouble(length -> (length - mean) * (length - mean))
            .average()
            .orElse(0.0);
        double coefficientOfVariation = Math.sqrt(variance) / mean;

        if (coefficientOfVariation >= LOW_CV) {
            return List.of();
        }

        boolean medium = coefficientOfVariation < MEDIUM_CV;
        return List.of(evidence()
            .severity(medium ? Evidence.Severity.MEDIUM : Evidence.Severity.LOW)
            .confidence(medium ? 0.6 : 0.5)
            .matchedValue(String.format("CV %.2f over %d sentences (mean %.1f words)",
                coefficientOfVariation, lengths.size(), mean))
            .explanation(String.format("Uniform sentence length (coefficient of variation %.2f across "
                + "%d sentences) - human writing is usually burstier", coefficientOfVariation, lengths.size()))
            .build());
    }
}
