package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util.PhraseResources;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * T10 - AI lexical markers.
 *
 * <p>High-frequency vocabulary disproportionately favored by chat models ("delve", "tapestry",
 * "leverage", "crucial"…), held in a tiered config lexicon. Matching is on whole word tokens (so
 * "realm" does not hit inside "overwhelm"); the canonical meme markers carry more weight than words
 * like "crucial"/"comprehensive" that appear in ordinary prose. A single common marker is ignored;
 * the signal is aggregate weighted density (per-1000-words), capped at MEDIUM since vocabulary
 * alone is circumstantial.
 */
@Component
public class LexicalMarkerRule extends TextAnalysisRule {
    private static final double TIER1_WEIGHT = 3.0;
    private static final double MEDIUM_PER_1000 = 5.0;

    private final Map<String, Double> markers;

    public LexicalMarkerRule(StaticAnalysisConfig config) {
        super("T10_AI_LEXICAL_MARKERS", Category.LEXICAL, config);
        this.markers = PhraseResources.loadWeighted("lexical/markers-en.txt", 1.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        Map<String, Integer> counts = new LinkedHashMap<>();
        for (String word : context.words()) {
            String token = word.toLowerCase();
            if (markers.containsKey(token)) {
                counts.merge(token, 1, Integer::sum);
            }
        }

        if (counts.isEmpty()) {
            return List.of();
        }

        boolean hasTier1 = counts.keySet().stream().anyMatch(marker -> markers.get(marker) >= TIER1_WEIGHT);
        int distinct = counts.size();
        if (!hasTier1 && distinct < 2) {
            return List.of();
        }

        double weightedHits = counts.entrySet().stream()
            .mapToDouble(entry -> markers.get(entry.getKey()) * entry.getValue())
            .sum();
        int words = Math.max(context.wordCount(), 1);
        double per1000 = weightedHits / words * 1000.0;

        boolean medium = per1000 >= MEDIUM_PER_1000 || (hasTier1 && distinct >= 2);
        Evidence.Severity severity = medium ? Evidence.Severity.MEDIUM : Evidence.Severity.LOW;

        List<Evidence> evidence = new ArrayList<>();

        for (Map.Entry<String, Integer> entry : counts.entrySet()) {
            String marker = entry.getKey();
            int count = entry.getValue();

            evidence.add(evidence()
                .severity(severity)
                .confidence(markers.get(marker) >= TIER1_WEIGHT ? 0.7 : 0.6)
                .matchedValue(marker + " (x" + count + ")")
                .explanation("AI-associated lexical marker \"" + marker + "\" used " + count + " time(s)")
                .build());
        }

        return evidence;
    }
}
