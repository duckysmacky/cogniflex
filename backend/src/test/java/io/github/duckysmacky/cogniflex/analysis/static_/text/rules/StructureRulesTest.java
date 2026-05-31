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

class StructureRulesTest {
    private static final int ROCKET = 0x1F680;
    private static final int SPARKLES = 0x2728;
    private static final int FIRE = 0x1F525;

    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    private final MarkdownStructureRule markdown = new MarkdownStructureRule();
    private final EmojiDecorationRule emoji = new EmojiDecorationRule();

    @Test
    void boldLeadInListItemsAreMedium() {
        RuleResult result = markdown.evaluate(context(
            "Here are the benefits:\n"
                + "- **Scalability:** it grows easily.\n"
                + "- **Performance:** it runs fast.\n"
                + "- **Reliability:** it stays up."));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void highListDensityIsMedium() {
        RuleResult result = markdown.evaluate(context(
            "Steps:\n- first\n- second\n- third\n- fourth"));

        assertTrue(result.matched());
        assertTrue(result.evidence().stream().anyMatch(e -> e.severity() == Evidence.Severity.MEDIUM));
    }

    @Test
    void plainProseHasNoMarkdownStructure() {
        RuleResult result = markdown.evaluate(context(
            "This is a normal paragraph without any formatting at all. It simply has two sentences."));

        assertFalse(result.matched());
    }

    @Test
    void emojiDecoratedHeadingsAreMedium() {
        RuleResult result = emoji.evaluate(context(
            codePoint(ROCKET) + " Getting Started\nSome intro text here.\n"
                + codePoint(SPARKLES) + " Features\nMore text below."));

        assertTrue(result.matched());
        assertEquals(Evidence.Severity.MEDIUM, result.evidence().get(0).severity());
    }

    @Test
    void singleEmojiBulletIsLow() {
        RuleResult result = emoji.evaluate(context(
            "- " + codePoint(FIRE) + " Hot tip here\nA normal line follows."));

        assertTrue(result.matched());
        assertEquals(Evidence.Severity.LOW, result.evidence().get(0).severity());
    }

    @Test
    void plainHeadingsAreNotEmojiDecorated() {
        RuleResult result = emoji.evaluate(context("A normal heading\nWith text below it here."));

        assertFalse(result.matched());
    }

    private static String codePoint(int codePoint) {
        return new StringBuilder().appendCodePoint(codePoint).toString();
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
