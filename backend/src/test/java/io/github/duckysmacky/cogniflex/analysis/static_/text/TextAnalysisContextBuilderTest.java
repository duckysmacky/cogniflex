package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.processing.text.HiddenCharacterScanner;
import io.github.duckysmacky.cogniflex.processing.text.MatchTextNormalizer;
import io.github.duckysmacky.cogniflex.processing.text.ParagraphSplitter;
import io.github.duckysmacky.cogniflex.processing.text.SentenceSegmenter;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextAnalysisContextBuilderTest {
    private final TextAnalysisContextBuilder builder = new TextAnalysisContextBuilder(
        new HiddenCharacterScanner(),
        new SentenceSegmenter(),
        new WordTokenizer(),
        new ParagraphSplitter(),
        new MatchTextNormalizer()
    );

    @Test
    void buildPopulatesDerivedFieldsFromNormalizedText() {
        ContentItem item = textItem("First sentence here. Second one too!\n\nNew paragraph.");

        TextAnalysisContext context = builder.build(item);

        assertEquals(2, context.paragraphs().size());
        assertEquals(3, context.sentences().size());
        assertTrue(context.wordCount() >= 7);
        assertEquals(context.words().size(), context.wordCount());
        assertEquals(context.sentences().size(), context.sentenceCount());
    }

    @Test
    void buildComputesNfkcCasefoldedMatchText() {
        // Fullwidth "AI" plus a curly apostrophe should fold to plain lowercase ascii
        ContentItem item = textItem("ＡＩ can’t");

        TextAnalysisContext context = builder.build(item);

        assertTrue(context.matchText().contains("ai can"));
    }

    @Test
    void buildRejectsNonTextContent() {
        ContentItem item = new ContentItem(ContentType.IMAGE, null, "a.png", new byte[]{1}, Map.of());

        assertThrows(IllegalArgumentException.class, () -> builder.build(item));
    }

    @Test
    void buildRejectsTextItemWithoutAnalyzableText() {
        ContentItem item = new ContentItem(ContentType.TEXT, null, null, null, Map.of());

        assertThrows(IllegalArgumentException.class, () -> builder.build(item));
    }

    private ContentItem textItem(String normalizedText) {
        return new ContentItem(
            ContentType.TEXT,
            null,
            null,
            null,
            Map.of(ContentItemFactory.NORMALIZED_TEXT_ATTRIBUTE, normalizedText)
        );
    }
}
