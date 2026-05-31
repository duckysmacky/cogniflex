package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T17 - Repetitive sentence openers / paragraph templating.
 *
 * <p>Models reuse the same opening word across many sentences ("Additionally,", "However,",
 * "It's important…"). The rule extracts the first word of each sentence and flags either a single
 * opener dominating a large share of sentences (MEDIUM) or generally low opener variety (LOW). Only
 * fires on a sufficiently long sample.
 */
@Component
public class RepetitiveOpenerRule extends TextAnalysisRule {
    private static final Pattern FIRST_WORD = Pattern.compile("^\\W*([\\p{L}']+)");

    private static final int MIN_SENTENCES = 6;
    private static final double DOMINANT_RATIO = 0.4;
    private static final double LOW_VARIETY_RATIO = 0.5;

    public RepetitiveOpenerRule() {
        super("T17_REPETITIVE_OPENERS", Category.STYLOMETRY, 10.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        List<String> sentences = context.sentences();
        if (sentences.size() < MIN_SENTENCES) {
            return List.of();
        }

        Map<String, Integer> openerCounts = new LinkedHashMap<>();
        int counted = 0;
        for (String sentence : sentences) {
            Matcher matcher = FIRST_WORD.matcher(sentence);
            if (matcher.find()) {
                openerCounts.merge(matcher.group(1).toLowerCase(), 1, Integer::sum);
                counted++;
            }
        }

        if (counted < MIN_SENTENCES) {
            return List.of();
        }

        Map.Entry<String, Integer> dominant = openerCounts.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElseThrow();
        double dominantRatio = (double) dominant.getValue() / counted;
        double distinctRatio = (double) openerCounts.size() / counted;

        if (dominant.getValue() >= 2 && dominantRatio >= DOMINANT_RATIO) {
            return List.of(evidence()
                .severity(Evidence.Severity.MEDIUM)
                .confidence(0.55)
                .matchedValue("\"" + dominant.getKey() + "\" opens " + dominant.getValue() + "/" + counted
                    + " sentences")
                .explanation("Repetitive sentence opener: \"" + dominant.getKey() + "\" begins "
                    + dominant.getValue() + " of " + counted + " sentences")
                .build());
        }

        if (distinctRatio <= LOW_VARIETY_RATIO) {
            return List.of(evidence()
                .severity(Evidence.Severity.LOW)
                .confidence(0.45)
                .matchedValue(openerCounts.size() + " distinct openers across " + counted + " sentences")
                .explanation("Low sentence-opener variety (" + openerCounts.size() + " distinct openers for "
                    + counted + " sentences)")
                .build());
        }

        return List.of();
    }
}
