package io.github.duckysmacky.cogniflex.analysis.static_.text.rules;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.Evidence;
import io.github.duckysmacky.cogniflex.analysis.static_.RuleResult;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContext;
import io.github.duckysmacky.cogniflex.analysis.static_.text.TextAnalysisContextBuilder;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacterScanner;
import io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer;
import io.github.duckysmacky.cogniflex.processing.text.ParagraphSplitter;
import io.github.duckysmacky.cogniflex.processing.text.SentenceSegmenter;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HiddenCharacterRulesTest {
    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    private final ZeroWidthCharacterRule zeroWidth = new ZeroWidthCharacterRule();
    private final SteganographyRule steganography = new SteganographyRule();

    @Test
    void zeroWidthRunIsFlaggedHigh() {
        RuleResult result = zeroWidth.evaluate(context("hello" + "​".repeat(5) + "world"));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.HIGH));
    }

    @Test
    void straySingleZeroWidthIsLow() {
        RuleResult result = zeroWidth.evaluate(context("a​b"));

        assertTrue(result.matched());
        assertEquals(1, result.evidence().size());
        assertEquals(Evidence.Severity.LOW, result.evidence().get(0).severity());
    }

    @Test
    void zeroWidthRuleIgnoresCleanText() {
        RuleResult result = zeroWidth.evaluate(context("Just some perfectly normal text here."));

        assertFalse(result.matched());
    }

    @Test
    void tagCharacterPayloadIsRecoveredAsCritical() {
        // "x" followed by tag characters spelling "AI" (U+E0041, U+E0049)
        RuleResult result = steganography.evaluate(context(codePoints("x", 0xE0041, 0xE0049)));

        assertTrue(result.matched());
        assertEquals(Evidence.Severity.CRITICAL, result.evidence().get(0).severity());
        assertTrue(result.evidence().get(0).matchedValue().contains("AI"));
    }

    @Test
    void variationSelectorRunIsFlaggedCritical() {
        RuleResult result = steganography.evaluate(context(codePoints("x", 0xFE00, 0xFE01)));

        assertTrue(result.matched());
        assertEquals(Evidence.Severity.CRITICAL, result.evidence().get(0).severity());
    }

    @Test
    void singleVariationSelectorIsNotFlagged() {
        // A lone selector (e.g. emoji presentation) must not trigger steganography
        RuleResult result = steganography.evaluate(context(codePoints("x", 0xFE0F)));

        assertFalse(result.matched());
    }

    @Test
    void steganographyIgnoresCleanText() {
        assertFalse(steganography.evaluate(context("Just some perfectly normal text here.")).matched());
    }

    private static String codePoints(String prefix, int... codePoints) {
        StringBuilder builder = new StringBuilder(prefix);
        for (int codePoint : codePoints) {
            builder.appendCodePoint(codePoint);
        }
        return builder.toString();
    }

    private TextAnalysisContext context(String text) {
        ContentItem item = new ContentItem(
            ContentType.TEXT,
            null,
            null,
            null,
            Map.of(ContentItemFactory.NORMALIZED_TEXT_ATTRIBUTE, text)
        );
        return builder.build(item);
    }
}
