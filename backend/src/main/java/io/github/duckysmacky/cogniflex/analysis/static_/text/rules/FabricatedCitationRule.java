package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * T19 - Fabricated-citation patterns.
 *
 * <p>AI invents authoritative-looking references. Fabrication cannot be confirmed statically, but
 * format anomalies can: an ISBN whose checksum fails, an arXiv id with an impossible month, and a
 * cluster of inline author-year citations with no resolvable target (no URL/DOI anywhere). The
 * checksum/format anomalies are MEDIUM (concretely verifiable); the citations-without-targets
 * heuristic is a LOW co-signal.
 */
@Component
public class FabricatedCitationRule extends TextAnalysisRule {
    private static final Pattern ISBN =
        Pattern.compile("(?i)\\bISBN(?:-1[03])?:?\\s*([0-9X][0-9X\\- ]{8,16}[0-9X])");
    private static final Pattern ARXIV =
        Pattern.compile("(?i)\\barxiv:\\s*(\\d{2})(\\d{2})\\.\\d{4,5}");
    private static final Pattern AUTHOR_YEAR =
        Pattern.compile("\\([A-Z][^()\\n]{1,60},\\s*(?:1[89]|20)\\d{2}[a-z]?\\)");
    private static final Pattern RESOLVABLE_TARGET =
        Pattern.compile("(?i)(?:https?://|\\bdoi:|\\b10\\.\\d{4,9}/)");

    private static final int CITATION_CLUSTER = 5;

    public FabricatedCitationRule(StaticAnalysisConfig config) {
        super("T19_FABRICATED_CITATIONS", Category.SEMANTIC, config);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.text();
        List<Evidence> evidence = new ArrayList<>();

        Matcher isbn = ISBN.matcher(text);
        while (isbn.find()) {
            if (!isValidIsbn(isbn.group(1))) {
                evidence.add(evidence()
                    .severity(Evidence.Severity.MEDIUM)
                    .confidence(0.7)
                    .matchedValue(isbn.group().strip())
                    .explanation("ISBN with an invalid checksum/length: \"" + isbn.group().strip() + "\"")
                    .build());
            }
        }

        Matcher arxiv = ARXIV.matcher(text);
        while (arxiv.find()) {
            int month = Integer.parseInt(arxiv.group(2));
            if (month < 1 || month > 12) {
                evidence.add(evidence()
                    .severity(Evidence.Severity.MEDIUM)
                    .confidence(0.6)
                    .matchedValue(arxiv.group().strip())
                    .explanation("Malformed arXiv identifier (impossible month): \"" + arxiv.group().strip() + "\"")
                    .build());
            }
        }

        int citations = countMatches(text, AUTHOR_YEAR);
        if (citations >= CITATION_CLUSTER && !RESOLVABLE_TARGET.matcher(text).find()) {
            evidence.add(evidence()
                .severity(Evidence.Severity.LOW)
                .confidence(0.4)
                .matchedValue(citations + " author-year citations, no URL/DOI")
                .explanation("Cluster of " + citations + " inline author-year citations with no resolvable "
                    + "target (no URL or DOI present)")
                .build());
        }

        return evidence;
    }

    private boolean isValidIsbn(String raw) {
        String digits = raw.replaceAll("[^0-9Xx]", "").toUpperCase();
        if (digits.length() == 10) {
            return isValidIsbn10(digits);
        }
        if (digits.length() == 13) {
            return isValidIsbn13(digits);
        }
        return false;
    }

    private boolean isValidIsbn10(String digits) {
        int sum = 0;
        for (int i = 0; i < 10; i++) {
            char c = digits.charAt(i);
            int value;
            if (c == 'X') {
                if (i != 9) {
                    return false;
                }
                value = 10;
            } else if (Character.isDigit(c)) {
                value = c - '0';
            } else {
                return false;
            }
            sum += value * (10 - i);
        }
        return sum % 11 == 0;
    }

    private boolean isValidIsbn13(String digits) {
        int sum = 0;
        for (int i = 0; i < 13; i++) {
            char c = digits.charAt(i);
            if (!Character.isDigit(c)) {
                return false;
            }
            int value = c - '0';
            sum += (i % 2 == 0) ? value : value * 3;
        }
        return sum % 10 == 0;
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
