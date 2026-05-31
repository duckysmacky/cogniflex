package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
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
 * T2 — Conversational scaffold leakage.
 *
 * <p>Politeness / framing boilerplate that survives a lazy copy-paste: enthusiastic openers at the
 * start of a line ("Certainly!", "Great question!"), "here is the ... you requested" framings, and
 * closers ("I hope this helps", "Let me know if you need…", "Is there anything else…"). Weaker than
 * T1 because humans occasionally write these, so HIGH severity with moderate confidence and a
 * weight well below the direct-leakage rule. The signal strengthens as distinct scaffolds co-occur,
 * which the per-evidence aggregation already rewards.
 */
@Component
public class ConversationalScaffoldRule extends TextAnalysisRule {
    private static final Pattern OPENER = Pattern.compile(
        "(?m)^(?:certainly|sure|absolutely|of course|great question|good question|"
            + "great choice|happy to help|no problem)\\s*[!.,:]"
    );
    private static final Pattern REQUESTED = Pattern.compile(
        "\\bhere (?:is|are) (?:the |your )?[^.\\n]{0,40}\\byou(?:'ve| have)? requested"
    );
    private static final Pattern CLOSER = Pattern.compile(
        "\\b(?:i hope this helps"
            + "|let me know if you (?:have|need|'d like|would like|want)"
            + "|feel free to (?:ask|reach out|let me know|modify|adjust)"
            + "|is there anything else"
            + "|let me know if there'?s anything"
            + "|hope this helps)\\b"
    );

    public ConversationalScaffoldRule() {
        super("T2_CONVERSATIONAL_SCAFFOLD", Category.LEAKAGE, 25.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.matchText();
        Set<String> hits = new LinkedHashSet<>();
        addHits(text, OPENER, hits);
        addHits(text, REQUESTED, hits);
        addHits(text, CLOSER, hits);

        List<Evidence> evidence = new ArrayList<>();
        for (String hit : hits) {
            evidence.add(evidence()
                .severity(Evidence.Severity.CRITICAL)
                .confidence(0.7)
                .matchedValue(hit)
                .explanation("Conversational assistant scaffold: \"" + hit + "\"")
                .build());
        }
        return evidence;
    }

    private void addHits(String text, Pattern pattern, Set<String> hits) {
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            hits.add(matcher.group().strip());
        }
    }
}
