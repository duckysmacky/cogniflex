package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T4 — Placeholder leakage.
 *
 * <p>Unfilled template fragments the user forgot to edit out of generated content:
 * {@code [insert X]}, {@code [Your Name]}, {@code <your text here>}, {@code (add details here)},
 * and the canonical {@code example.com} / {@code lorem ipsum} fillers. Bracket/angle placeholders
 * are HIGH (humans rarely leave them); {@code example.com} and lorem ipsum are MEDIUM since they
 * have legitimate uses.
 */
@Component
public class PlaceholderLeakageRule extends TextAnalysisRule {
    private record PlaceholderPattern(
        Pattern pattern,
        Evidence.Severity severity,
        double confidence
    ) {}

    private static final List<PlaceholderPattern> PATTERNS = List.of(
        new PlaceholderPattern(Pattern.compile(
            "\\[(?:insert|your|company|client|product|name|date|topic|brand|website|url|email|phone|address|x)\\b[^\\]]*\\]"
        ), Evidence.Severity.HIGH, 0.85),
        new PlaceholderPattern(Pattern.compile(
            "\\[[^\\]\\n]*(?:name|here|date|address|number)\\]"
        ), Evidence.Severity.HIGH, 0.8),
        new PlaceholderPattern(Pattern.compile(
            "<(?:your|insert)[^>\\n]*>"
        ), Evidence.Severity.HIGH, 0.85),
        new PlaceholderPattern(Pattern.compile(
            "\\((?:insert|add)[^)\\n]*\\)"
        ), Evidence.Severity.HIGH, 0.75),
        new PlaceholderPattern(Pattern.compile(
            "\\b(?:[a-z0-9._%+-]+@)?example\\.(?:com|org|net)\\b"
        ), Evidence.Severity.MEDIUM, 0.6),
        new PlaceholderPattern(Pattern.compile(
            "\\blorem ipsum\\b"
        ), Evidence.Severity.MEDIUM, 0.55)
    );

    public PlaceholderLeakageRule(StaticAnalysisConfig config) {
        super("T4_PLACEHOLDER_LEAKAGE", Category.LEAKAGE, config);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.matchText();
        Set<String> seen = new LinkedHashSet<>();
        List<Evidence> evidence = new ArrayList<>();

        for (PlaceholderPattern placeholder : PATTERNS) {
            Matcher matcher = placeholder.pattern().matcher(text);
            while (matcher.find()) {
                String hit = matcher.group().strip();
                if (!seen.add(hit)) {
                    continue;
                }
                evidence.add(evidence()
                    .severity(placeholder.severity())
                    .confidence(placeholder.confidence())
                    .matchedValue(hit)
                    .explanation("Unfilled placeholder / template filler: \"" + hit + "\"")
                    .build());
            }
        }
        return evidence;
    }
}
