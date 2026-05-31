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
 * T12 - Rhetorical-template constructions.
 *
 * <p>Syntactic templates models overuse: negative parallelism ("it's not just X, it's Y"),
 * "this isn't about X. it's about Y", "whether you're a … or a …,", "from X to Y," openers,
 * "in a world where", "in the realm of", and the rule-of-three list. These are regex-detectable
 * but carry a higher false-positive risk than fixed phrases, so confidence per hit is moderate and
 * the weight is small.
 */
@Component
public class RhetoricalTemplateRule extends TextAnalysisRule {
    private record Template(Pattern pattern, Evidence.Severity severity, double confidence, String label) {
    }

    private static final List<Template> TEMPLATES = List.of(
        new Template(Pattern.compile(
            "\\bit's not (?:just )?[^,.\\n]{1,40}[,.]\\s+it's [^.!?\\n]{1,40}[.!?]"
        ), Evidence.Severity.MEDIUM, 0.55, "negative parallelism (\"it's not X, it's Y\")"),
        new Template(Pattern.compile(
            "\\bthis isn't about [^.!?\\n]{1,50}[.!?]\\s+it's about [^.!?\\n]{1,50}[.!?]"
        ), Evidence.Severity.MEDIUM, 0.55, "antithesis (\"this isn't about X. it's about Y\")"),
        new Template(Pattern.compile(
            "\\bwhether you're (?:an? )?[^,.\\n]{1,40} or (?:an? )?[^,.\\n]{1,40},"
        ), Evidence.Severity.MEDIUM, 0.5, "\"whether you're a … or a …,\""),
        new Template(Pattern.compile(
            "\\bin a world where\\b"
        ), Evidence.Severity.MEDIUM, 0.5, "\"in a world where …\""),
        new Template(Pattern.compile(
            "\\bin the realm of\\b"
        ), Evidence.Severity.MEDIUM, 0.5, "\"in the realm of …\""),
        new Template(Pattern.compile(
            "\\bfrom [^,.\\n]{1,30} to [^,.\\n]{1,30},"
        ), Evidence.Severity.LOW, 0.4, "\"from X to Y,\" enumeration"),
        new Template(Pattern.compile(
            "\\b\\w+, \\w+, and \\w+\\b"
        ), Evidence.Severity.LOW, 0.4, "rule-of-three list")
    );

    public RhetoricalTemplateRule(StaticAnalysisConfig config) {
        super("T12_RHETORICAL_TEMPLATES", Category.PHRASING, config);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.matchText();
        Set<String> seen = new LinkedHashSet<>();
        List<Evidence> evidence = new ArrayList<>();

        for (Template template : TEMPLATES) {
            Matcher matcher = template.pattern().matcher(text);

            while (matcher.find()) {
                String hit = matcher.group().strip();

                if (!seen.add(hit)) {
                    continue;
                }

                evidence.add(evidence()
                    .severity(template.severity())
                    .confidence(template.confidence())
                    .matchedValue(hit)
                    .explanation("Rhetorical template - " + template.label() + ": \"" + hit + "\"")
                    .build());
            }
        }

        return evidence;
    }
}
