package io.github.duckysmacky.cogniflex.processing.text;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Scans a string for hidden / invisible / formatting code points (zero-width characters,
 * bidi controls, variation selectors, tag characters, soft hyphens, BOMs, etc.) and reports
 * each occurrence with its position.
 *
 * <p>Shared by {@link TextPreprocessor} (preprocessing forensics) and the static text analysis
 * context builder, so both observe hidden characters through the same classification logic
 * ({@link HiddenCharacter.Kind#fromCodePoint(int)}).
 */
@Component
public class HiddenCharacterScanner {
    public List<HiddenCharacter> scan(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        List<HiddenCharacter> characters = new ArrayList<>();
        int charIndex = 0;
        int codePointIndex = 0;

        while (charIndex < text.length()) {
            int codePoint = text.codePointAt(charIndex);
            HiddenCharacter.Kind kind = HiddenCharacter.Kind.fromCodePoint(codePoint);

            if (kind != null) {
                characters.add(new HiddenCharacter(
                    codePoint,
                    charIndex,
                    codePointIndex,
                    kind,
                    notation(codePoint)
                ));
            }

            charIndex += Character.charCount(codePoint);
            codePointIndex++;
        }

        return List.copyOf(characters);
    }

    private String notation(int codePoint) {
        return "U+" + String.format("%04X", codePoint);
    }
}
