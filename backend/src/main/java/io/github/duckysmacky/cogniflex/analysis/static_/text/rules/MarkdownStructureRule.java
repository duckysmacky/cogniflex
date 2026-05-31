package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T13 - Excessive Markdown / list / bold structure.
 *
 * <p>Models over-format. The rule measures structural density: bold-lead-in list items
 * ({@code - **Scalability:** …}) - a very strong AI list template - plus list-line density and raw
 * {@code **bold**} count. Plain prose produces nothing; heavy Markdown raises MEDIUM evidence
 * (legitimate READMEs format this way too, hence the small weight and reliance on the combination
 * bonus).
 */
@Component
public class MarkdownStructureRule extends TextAnalysisRule {
    private static final Pattern LIST_ITEM = Pattern.compile("^\\s*(?:[-*+]|\\d+\\.)\\s+\\S");
    private static final Pattern BOLD_LEAD_IN = Pattern.compile("^\\s*[-*+]\\s+\\*\\*[^*\\n]+\\*\\*");
    private static final Pattern BOLD_RUN = Pattern.compile("\\*\\*[^*\\n]+\\*\\*");

    private static final int BOLD_LEAD_IN_MEDIUM = 2;
    private static final int BOLD_LEAD_IN_HIGH = 5;
    private static final int MIN_LINES_FOR_DENSITY = 5;
    private static final int MIN_LIST_LINES = 3;
    private static final double LIST_RATIO = 0.4;
    private static final int BOLD_RUN_MIN = 5;

    public MarkdownStructureRule(StaticAnalysisConfig config) {
        super("T13_MARKDOWN_STRUCTURE", Category.STRUCTURE, config);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        int listLines = 0;
        int boldLeadIns = 0;
        int nonBlankLines = 0;

        for (String line : context.lines()) {
            if (line.isBlank()) {
                continue;
            }
            nonBlankLines++;
            if (LIST_ITEM.matcher(line).find()) {
                listLines++;
            }
            if (BOLD_LEAD_IN.matcher(line).find()) {
                boldLeadIns++;
            }
        }

        int boldRuns = countMatches(context.text(), BOLD_RUN);
        List<Evidence> evidence = new ArrayList<>();

        if (boldLeadIns >= BOLD_LEAD_IN_MEDIUM) {
            evidence.add(evidence()
                .severity(boldLeadIns >= BOLD_LEAD_IN_HIGH ? Evidence.Severity.HIGH : Evidence.Severity.MEDIUM)
                .confidence(0.6)
                .matchedValue(boldLeadIns + " bold lead-in list items")
                .explanation(boldLeadIns + " \"- **Label:** …\" bold lead-in list items — a strong "
                    + "AI list template")
                .build());
        }

        double listRatio = nonBlankLines == 0 ? 0.0 : (double) listLines / nonBlankLines;
        if (nonBlankLines >= MIN_LINES_FOR_DENSITY && listLines >= MIN_LIST_LINES && listRatio >= LIST_RATIO) {
            evidence.add(evidence()
                .severity(Evidence.Severity.MEDIUM)
                .confidence(0.55)
                .matchedValue(listLines + "/" + nonBlankLines + " lines are list items")
                .explanation("High list-item density (" + listLines + " of " + nonBlankLines
                    + " non-blank lines, " + formatPercent(listRatio) + ")")
                .build());
        }

        if (boldRuns >= BOLD_RUN_MIN) {
            evidence.add(evidence()
                .severity(Evidence.Severity.LOW)
                .confidence(0.45)
                .matchedValue(boldRuns + " bold runs")
                .explanation("Heavy inline bold usage (" + boldRuns + " **bold** spans)")
                .build());
        }

        return evidence;
    }

    private int countMatches(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }

    private String formatPercent(double ratio) {
        return String.format("%.0f%%", ratio * 100);
    }
}
