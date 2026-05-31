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
 * T9 - Consistent "smart" typography.
 *
 * <p>Models emit typographically perfect text where casual humans type ASCII: curly quotes instead
 * of {@code "}/{@code '}, the ellipsis character {@code …} instead of {@code ...}. The signal is
 * <em>consistency</em>, not presence - humans mix straight and curly, so the rule flags
 * near-perfect consistency (curly punctuation with <em>zero</em> straight equivalents) above a
 * minimum volume, never a single curly mark. LOW–MEDIUM weight.
 */
@Component
public class SmartTypographyConsistencyRule extends TextAnalysisRule {
    private static final Pattern LITERAL_ELLIPSIS = Pattern.compile("\\.\\.\\.");

    private static final int MIN_APOSTROPHES = 3;
    private static final int MIN_QUOTES = 2;
    private static final int MEDIUM_ELLIPSIS = 3;

    public SmartTypographyConsistencyRule(StaticAnalysisConfig config) {
        super("T9_SMART_TYPOGRAPHY", Category.TYPOGRAPHY, config);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.text();
        List<Evidence> evidence = new ArrayList<>();

        int straightApostrophes = countChar(text, '\'');
        int curlyApostrophes = countChar(text, '‘') + countChar(text, '’');
        if (curlyApostrophes >= MIN_APOSTROPHES && straightApostrophes == 0) {
            evidence.add(evidence()
                .severity(Evidence.Severity.MEDIUM)
                .confidence(0.5)
                .matchedValue(curlyApostrophes + " curly / 0 straight apostrophes")
                .explanation("All " + curlyApostrophes + " apostrophes are typographic (’) with no "
                    + "straight apostrophes — consistent with generated output")
                .build());
        }

        int straightQuotes = countChar(text, '"');
        int curlyQuotes = countChar(text, '“') + countChar(text, '”');
        if (curlyQuotes >= MIN_QUOTES && straightQuotes == 0) {
            evidence.add(evidence()
                .severity(Evidence.Severity.MEDIUM)
                .confidence(0.5)
                .matchedValue(curlyQuotes + " curly / 0 straight double quotes")
                .explanation("All " + curlyQuotes + " double quotes are typographic (“ ”) with no "
                    + "straight quotes — consistent with generated output")
                .build());
        }

        int ellipsisChars = countChar(text, '…');
        int literalEllipses = countMatches(text, LITERAL_ELLIPSIS);
        if (ellipsisChars >= 1 && literalEllipses == 0) {
            evidence.add(evidence()
                .severity(ellipsisChars >= MEDIUM_ELLIPSIS ? Evidence.Severity.MEDIUM : Evidence.Severity.LOW)
                .confidence(0.4)
                .matchedValue(ellipsisChars + " ellipsis character(s), 0 literal \"...\"")
                .explanation("Uses the ellipsis character (…) rather than \"...\" (" + ellipsisChars
                    + " occurrence(s))")
                .build());
        }

        return evidence;
    }

    private int countChar(String text, char target) {
        int count = 0;
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i) == target) {
                count++;
            }
        }
        return count;
    }

    private int countMatches(String text, Pattern pattern) {
        Matcher matcher = pattern.matcher(text);
        int count = 0;
        while (matcher.find()) {
            count++;
        }
        return count;
    }
}
