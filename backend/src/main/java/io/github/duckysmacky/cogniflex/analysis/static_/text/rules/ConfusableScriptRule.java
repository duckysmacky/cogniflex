package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import com.ibm.icu.lang.UScript;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * T7 - Unicode confusables / mixed-script anomalies.
 *
 * <p>Homoglyph tricks mix lookalike letters from another script into otherwise-Latin words
 * (Cyrillic а/е/о/с/р, Greek lookalikes). The rule scans runs of letters and flags any single word
 * that mixes Latin with Cyrillic or Greek - a near-impossible accident in genuine prose. Weak on
 * its own (also a phishing/spam signal), so LOW–MEDIUM and useful mainly as a co-signal.
 */
@Component
public class ConfusableScriptRule extends TextAnalysisRule {
    private static final int MEDIUM_THRESHOLD = 2;

    public ConfusableScriptRule() {
        super("T7_CONFUSABLE_SCRIPT", Category.HIDDEN_CHARACTERS, 6.0);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        String text = context.text();
        int mixedWords = 0;
        String sample = null;

        int index = 0;
        int length = text.length();
        while (index < length) {
            int codePoint = text.codePointAt(index);
            if (!Character.isLetter(codePoint)) {
                index += Character.charCount(codePoint);
                continue;
            }

            int start = index;
            Set<Integer> scripts = new HashSet<>();
            while (index < length) {
                int letter = text.codePointAt(index);
                if (!Character.isLetter(letter)) {
                    break;
                }
                addScript(scripts, letter);
                index += Character.charCount(letter);
            }

            if (isConfusable(scripts)) {
                mixedWords++;
                if (sample == null) {
                    sample = text.substring(start, index);
                }
            }
        }

        if (mixedWords == 0) {
            return List.of();
        }

        Evidence.Severity severity = mixedWords >= MEDIUM_THRESHOLD
            ? Evidence.Severity.MEDIUM
            : Evidence.Severity.LOW;

        return List.of(evidence()
            .severity(severity)
            .confidence(0.6)
            .matchedValue(mixedWords + " mixed-script word(s), e.g. \"" + sample + "\"")
            .explanation(mixedWords + " word(s) mix Latin with Cyrillic/Greek lookalike letters, e.g. \""
                + sample + "\" — a homoglyph anomaly")
            .build());
    }

    private void addScript(Set<Integer> scripts, int codePoint) {
        int script = UScript.getScript(codePoint);
        if (script != UScript.COMMON && script != UScript.INHERITED && script != UScript.UNKNOWN) {
            scripts.add(script);
        }
    }

    private boolean isConfusable(Set<Integer> scripts) {
        return scripts.contains(UScript.LATIN)
            && (scripts.contains(UScript.CYRILLIC) || scripts.contains(UScript.GREEK));
    }
}
