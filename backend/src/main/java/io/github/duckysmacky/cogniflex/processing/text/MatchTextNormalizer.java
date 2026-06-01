package io.github.duckysmacky.cogniflex.processing.text;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;
import com.ibm.icu.text.Normalizer2;
import org.springframework.stereotype.Component;

/**
 * Produces an aggressively normalized form of a text intended for phrase / lexicon matching that
 * is hard to defeat with cosmetic obfuscation.
 *
 * <p>The transformation is:
 * <ol>
 *     <li>NFKC case-folding (collapses compatibility variants — e.g. fullwidth letters — and
 *         lowercases);</li>
 *     <li>removal of default-ignorable code points (zero-width spaces/joiners, soft hyphens,
 *         variation selectors, tag characters, bidi controls) so invisible characters inserted
 *         between letters cannot break a match;</li>
 *     <li>folding of "smart" quotes and apostrophes to their ASCII equivalents so curly-quote
 *         output matches phrase libraries written with straight quotes.</li>
 * </ol>
 *
 * Newlines are preserved so callers can still anchor matches to line boundaries.
 */
@Component
public class MatchTextNormalizer {
    private static final Normalizer2 NFKC_CASEFOLD = Normalizer2.getNFKCCasefoldInstance();

    public String normalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }

        String folded = NFKC_CASEFOLD.normalize(text);
        StringBuilder builder = new StringBuilder(folded.length());

        folded.codePoints().forEach(codePoint -> {
            if (UCharacter.hasBinaryProperty(codePoint, UProperty.DEFAULT_IGNORABLE_CODE_POINT)) {
                return;
            }
            builder.appendCodePoint(foldQuote(codePoint));
        });

        return builder.toString();
    }

    private int foldQuote(int codePoint) {
        return switch (codePoint) {
            // single quotes / apostrophes / primes
            case 0x2018, 0x2019, 0x201A, 0x201B, 0x2032, 0x2035, 0x02BC -> '\'';
            // double quotes / guillemets / double primes
            case 0x201C, 0x201D, 0x201E, 0x201F, 0x2033, 0x2036, 0x00AB, 0x00BB -> '"';
            default -> codePoint;
        };
    }
}
