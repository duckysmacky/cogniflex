package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util.LiteralPhraseMatcher;
import io.github.duckysmacky.cogniflex.analysis.static_.text.rules.util.PhraseResources;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T1 — Direct prompt / assistant leakage.
 *
 * <p>The single strongest text signal: raw model output that carries the assistant's own
 * meta-commentary, self-identification, or a refusal template. Detection is a literal phrase set
 * (Aho-Corasick) plus a handful of templated regexes for the highly productive
 * "as an AI ..." / "I can't (assist|provide) ..." families. A single hit is near-proof, so each
 * piece of evidence is CRITICAL with very high detection confidence.
 */
@Component
public class DirectAiLeakageRule extends TextAnalysisRule {
    private static final Pattern AI_IDENTITY = Pattern.compile(
        "\\bas an? (?:ai|artificial intelligence)(?: language)?(?: model| assistant)?\\b"
    );
    private static final Pattern REFUSAL = Pattern.compile(
        "\\bi(?:'m| am)?\\s+(?:sorry,?\\s+)?(?:but\\s+)?(?:i\\s+)?can(?:no|')?t\\s+"
            + "(?:fulfil|fulfill|assist|provide|help|comply|complete)"
    );

    private final LiteralPhraseMatcher matcher;

    public DirectAiLeakageRule() {
        super("T1_DIRECT_AI_LEAKAGE", Category.LEAKAGE, 60.0);
        this.matcher = new LiteralPhraseMatcher(PhraseResources.load("leakage/direct-ai.txt"));
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.matchText();
        Set<String> hits = new LinkedHashSet<>(matcher.findDistinct(text));
        addRegexHits(text, AI_IDENTITY, hits);
        addRegexHits(text, REFUSAL, hits);

        List<Evidence> evidence = new ArrayList<>();
        for (String hit : hits) {
            evidence.add(evidence()
                .severity(Evidence.Severity.CRITICAL)
                .confidence(1.0)
                .matchedValue(hit)
                .explanation("Text contains assistant self-reference / refusal leakage: \"" + hit + "\"")
                .build());
        }
        return evidence;
    }

    private void addRegexHits(String text, Pattern pattern, Set<String> hits) {
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            hits.add(matcher.group().strip());
        }
    }
}
