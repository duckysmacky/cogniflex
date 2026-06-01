package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
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
 * T3 — Knowledge-cutoff / capability disclaimers.
 *
 * <p>"As of my last knowledge update…", "I don't have access to real-time data", "I cannot browse
 * the internet", "as of my training in 2023". Strong because humans essentially never volunteer
 * these, but kept at HIGH (not CRITICAL) since an article quoting an assistant could include them.
 */
@Component
public class KnowledgeCutoffRule extends TextAnalysisRule {
    private static final Pattern TRAINING_YEAR = Pattern.compile(
        "\\bas of (?:my (?:last )?training|my knowledge(?: cutoff)?|(?:early |late |mid )?20[12]\\d)\\b"
    );

    private final LiteralPhraseMatcher matcher;

    public KnowledgeCutoffRule(StaticAnalysisConfig config) {
        super("T3_KNOWLEDGE_CUTOFF", Category.LEAKAGE, config);
        this.matcher = new LiteralPhraseMatcher(PhraseResources.load("leakage/knowledge-cutoff.txt"));
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.matchText();
        Set<String> hits = new LinkedHashSet<>(matcher.findDistinct(text));
        Matcher yearMatcher = TRAINING_YEAR.matcher(text);
        while (yearMatcher.find()) {
            hits.add(yearMatcher.group().strip());
        }

        List<Evidence> evidence = new ArrayList<>();
        for (String hit : hits) {
            evidence.add(evidence()
                .severity(Evidence.Severity.CRITICAL)
                .confidence(0.85)
                .matchedValue(hit)
                .explanation("Knowledge-cutoff / capability disclaimer: \"" + hit + "\"")
                .build());
        }
        return evidence;
    }
}
