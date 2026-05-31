package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util.LiteralPhraseMatcher;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util.PhraseResources;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * T11 - AI transition / cliche phrases.
 *
 * <p>Multi-word collocations are a stronger tell than single words: "it's important to note",
 * "in today's fast-paced world", "a testament to", "navigating the complexities of", "dive into".
 * Strong collocations are MEDIUM; bland connectors ("furthermore", "moreover", "in conclusion")
 * are LOW. Matched with Aho-Corasick over the normalized match surface, so smart-quote /
 * zero-width obfuscation does not hide them. The score grows with the variety of distinct phrases.
 */
@Component
public class PhraseRule extends TextAnalysisRule {
    private static final double STRONG_WEIGHT = 2.0;

    private final Map<String, Double> phrases;
    private final LiteralPhraseMatcher matcher;

    public PhraseRule() {
        super("T11_AI_PHRASES", Category.PHRASING, 12.0);
        this.phrases = PhraseResources.loadWeighted("phrasing/transitions-en.txt", 1.0);
        this.matcher = new LiteralPhraseMatcher(phrases.keySet());
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        List<Evidence> evidence = new ArrayList<>();

        for (String phrase : matcher.findDistinct(context.matchText())) {
            boolean strong = phrases.getOrDefault(phrase, 1.0) >= STRONG_WEIGHT;

            evidence.add(evidence()
                .severity(strong ? Evidence.Severity.MEDIUM : Evidence.Severity.LOW)
                .confidence(strong ? 0.6 : 0.45)
                .matchedValue(phrase)
                .explanation("AI transition/cliche phrase: \"" + phrase + "\"")
                .build());
        }

        return evidence;
    }
}
