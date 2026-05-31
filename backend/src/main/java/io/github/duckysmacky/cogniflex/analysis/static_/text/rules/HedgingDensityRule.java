package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util.PhraseResources;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * T18 - Hedging / false-balance density.
 *
 * <p>AI text over-hedges ("generally", "typically", "in many cases", "it depends") and reflexively
 * frames both sides ("on the other hand"). The rule counts hedge terms per 1000 words and detects
 * both-sides framing. Individually weak, so LOW–MEDIUM severity with a small weight; needs enough
 * words before the density path engages.
 */
@Component
public class HedgingDensityRule extends TextAnalysisRule {
    private static final Pattern OTHER_HAND = Pattern.compile("\\bon the other hand\\b");

    private static final int MIN_WORDS_FOR_DENSITY = 50;
    private static final double MEDIUM_PER_1000 = 15.0;
    private static final double LOW_PER_1000 = 8.0;
    private static final int SHORT_TEXT_MIN_HITS = 3;

    private final Pattern hedgePattern;

    public HedgingDensityRule(StaticAnalysisConfig config) {
        super("T18_HEDGING_DENSITY", Category.STYLOMETRY, config);
        this.hedgePattern = buildHedgePattern();
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.matchText();
        List<Evidence> evidence = new ArrayList<>();

        int otherHand = countMatches(text, OTHER_HAND);
        if (otherHand >= 1) {
            evidence.add(evidence()
                .severity(Evidence.Severity.MEDIUM)
                .confidence(0.5)
                .matchedValue(otherHand + "x \"on the other hand\"")
                .explanation("Reflexive both-sides framing (\"on the other hand\" used " + otherHand
                    + " time(s))")
                .build());
        }

        int hedges = countMatches(text, hedgePattern);
        int words = Math.max(context.wordCount(), 1);
        double per1000 = (double) hedges / words * 1000.0;

        if (context.wordCount() >= MIN_WORDS_FOR_DENSITY) {
            if (per1000 >= MEDIUM_PER_1000) {
                evidence.add(hedgeDensityEvidence(Evidence.Severity.MEDIUM, 0.5, hedges, per1000));
            } else if (per1000 >= LOW_PER_1000) {
                evidence.add(hedgeDensityEvidence(Evidence.Severity.LOW, 0.45, hedges, per1000));
            }
        } else if (hedges >= SHORT_TEXT_MIN_HITS) {
            evidence.add(hedgeDensityEvidence(Evidence.Severity.LOW, 0.4, hedges, per1000));
        }

        return evidence;
    }

    private Evidence hedgeDensityEvidence(Evidence.Severity severity, double confidence, int hedges, double per1000) {
        return evidence()
            .severity(severity)
            .confidence(confidence)
            .matchedValue(hedges + " hedge terms (" + String.format("%.1f", per1000) + "/1000 words)")
            .explanation("Elevated hedging density (" + hedges + " hedge terms, "
                + String.format("%.1f", per1000) + " per 1000 words)")
            .build();
    }

    private int countMatches(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private static Pattern buildHedgePattern() {
        String alternation = PhraseResources.load("stylometry/hedges-en.txt").stream()
            .sorted(Comparator.comparingInt(String::length).reversed())
            .map(Pattern::quote)
            .collect(Collectors.joining("|"));
        return Pattern.compile("\\b(?:" + alternation + ")\\b");
    }
}
