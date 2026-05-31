package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T8 - Em-dash overuse.
 *
 * <p>One of the more reliable stylistic tells of current chat models. The signal is <em>density,
 * not presence</em>: a single em-dash is noise, but an em-dash every couple of sentences - usually
 * in the unspaced {@code word—word} form models prefer over the human {@code word — word} - is a
 * strong fingerprint. Severity scales with em-dashes per sentence; per the false-positive caveat
 * (essayists and house styles love em-dashes) the weight stays small and leans on the combination
 * bonus.
 */
@Component
public class EmDashDensityRule extends TextAnalysisRule {
    private static final char EM_DASH = '—';
    private static final Pattern LETTER_EN_DASH = Pattern.compile("(?<=\\p{L})\\u2013(?=\\p{L})");
    private static final Pattern UNSPACED_EM_DASH = Pattern.compile("(?<=\\w)\\u2014(?=\\w)");

    private static final int MIN_EM_DASHES = 1;
    private static final int MIN_SENTENCES_FOR_DENSITY = 3;
    private static final double HIGH_DENSITY = 0.15;
    private static final double MEDIUM_DENSITY = 0.05;

    public EmDashDensityRule() {
        super("T8_EM_DASH_DENSITY", Category.TYPOGRAPHY, 10.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.text();
        int emDashes = countChar(text, EM_DASH) + countMatches(text, LETTER_EN_DASH);
        if (emDashes < MIN_EM_DASHES) {
            return List.of();
        }

        int unspaced = countMatches(text, UNSPACED_EM_DASH) + countMatches(text, LETTER_EN_DASH);
        int sentences = Math.max(context.sentenceCount(), 1);
        double density = (double) emDashes / sentences;

        Evidence.Severity severity = severityFor(emDashes, context.sentenceCount(), density);
        double confidence = unspaced * 2 >= emDashes ? 0.7 : 0.55;

        return List.of(evidence()
            .severity(severity)
            .confidence(confidence)
            .matchedValue(emDashes + " em-dashes / " + context.sentenceCount() + " sentences"
                + " (density " + String.format("%.2f", density) + ", " + unspaced + " unspaced)")
            .explanation("Em-dash density of " + String.format("%.2f", density) + " per sentence ("
                + emDashes + " across " + context.sentenceCount() + " sentences, " + unspaced
                + " in the unspaced word—word form models favor)")
            .build());
    }

    private Evidence.Severity severityFor(int emDashes, int sentenceCount, double density) {
        if (sentenceCount < MIN_SENTENCES_FOR_DENSITY) {
            return Evidence.Severity.LOW;
        }
        if (density > HIGH_DENSITY) {
            return Evidence.Severity.HIGH;
        }
        if (density >= MEDIUM_DENSITY) {
            return Evidence.Severity.MEDIUM;
        }
        return Evidence.Severity.LOW;
    }

    private int countChar(String text, char target) {
        int count = 0;

        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == target) {
                count++;
            }
        }

        return count;
    }

    private int countMatches(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;

        while (matcher.find()) {
            count++;
        }

        return count;
    }
}
