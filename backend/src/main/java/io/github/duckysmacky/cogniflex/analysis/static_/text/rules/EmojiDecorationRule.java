package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisRule;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Pattern;

/**
 * T14 - Emoji-decorated headers / bullets.
 *
 * <p>Section headers prefixed with a topical emoji ("🚀 Getting Started", "✨ Features") and emoji
 * used as bullet glyphs are a strong stylistic tell of marketing-flavored AI output. The rule
 * counts lines whose first visible glyph (after any Markdown heading/list marker) is a pictographic
 * emoji followed by label text. One such line is LOW; several are MEDIUM.
 */
@Component
public class EmojiDecorationRule extends TextAnalysisRule {
    private static final Pattern LEADING_MARKER = Pattern.compile("^(?:#{1,6}\\s+|[-*+]\\s+|\\d+\\.\\s+)");

    private static final int MEDIUM_THRESHOLD = 2;

    public EmojiDecorationRule(StaticAnalysisConfig config) {
        super("T14_EMOJI_DECORATION", Category.STRUCTURE, config);
    }

    @Override
    protected List<Evidence> collectEvidence(TextAnalysisContext context) {
        int decorated = 0;
        String sample = null;

        for (String line : context.lines()) {
            String stripped = line.strip();
            if (stripped.isEmpty()) {
                continue;
            }

            String body = LEADING_MARKER.matcher(stripped).replaceFirst("");
            if (body.isEmpty()) {
                continue;
            }

            int firstCodePoint = body.codePointAt(0);
            if (isPictographic(firstCodePoint) && containsLetter(body)) {
                decorated++;
                if (sample == null) {
                    sample = stripped.length() > 40 ? stripped.substring(0, 40) + "…" : stripped;
                }
            }
        }

        if (decorated == 0) {
            return List.of();
        }

        Evidence.Severity severity = decorated >= MEDIUM_THRESHOLD
            ? Evidence.Severity.MEDIUM
            : Evidence.Severity.LOW;

        return List.of(evidence()
            .severity(severity)
            .confidence(decorated >= MEDIUM_THRESHOLD ? 0.55 : 0.45)
            .matchedValue(decorated + " emoji-decorated heading(s)/bullet(s), e.g. \"" + sample + "\"")
            .explanation(decorated + " heading/bullet line(s) prefixed with a topical emoji — a "
                + "marketing-flavoured AI formatting tell")
            .build());
    }

    private boolean isPictographic(int codePoint) {
        return UCharacter.hasBinaryProperty(codePoint, UProperty.EXTENDED_PICTOGRAPHIC);
    }

    private boolean containsLetter(String text) {
        return text.codePoints().anyMatch(Character::isLetter);
    }
}
