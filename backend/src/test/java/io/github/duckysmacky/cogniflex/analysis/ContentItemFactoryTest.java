package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.processing.media.ParsedMedia;
import io.github.duckysmacky.cogniflex.processing.text.ModelInputPreparer;
import io.github.duckysmacky.cogniflex.processing.text.PreprocessedText;
import io.github.duckysmacky.cogniflex.processing.text.TextPreprocessingOptions;
import io.github.duckysmacky.cogniflex.processing.text.TextPreprocessor;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ContentItemFactoryTest {
    private final ContentItemFactory factory = new ContentItemFactory();

    @Test
    void createsTextContentItemFromPreprocessedText() {
        PreprocessedText text = new TextPreprocessor(new ModelInputPreparer())
            .preprocess("Hello   world", TextPreprocessingOptions.forModelInput());

        ContentItem item = factory.fromText(text);

        assertEquals(ContentType.TEXT, item.contentType());
        assertEquals("Hello world", item.attributes().get(ContentItemFactory.TEXT_ATTRIBUTE));
        assertEquals("Hello world", item.attributes().get(ContentItemFactory.NORMALIZED_TEXT_ATTRIBUTE));
        assertEquals("0", item.attributes().get(ContentItemFactory.HIDDEN_CHARACTERS_ATTRIBUTE));
    }

    @Test
    void createsMediaContentItemFromParsedMedia() {
        byte[] bytes = new byte[] {1, 2, 3};
        ParsedMedia media = new ParsedMedia(
            MediaType.IMAGE,
            bytes,
            "sample.png",
            "image/png",
            bytes.length,
            "png"
        );

        ContentItem item = factory.fromMedia(media);

        assertEquals(ContentType.IMAGE, item.contentType());
        assertEquals("sample.png", item.filename());
        assertArrayEquals(bytes, item.bytes());
        assertEquals("image/png", item.attributes().get(ContentItemFactory.CONTENT_TYPE_ATTRIBUTE));
        assertEquals("3", item.attributes().get(ContentItemFactory.SIZE_ATTRIBUTE));
        assertEquals("png", item.attributes().get(ContentItemFactory.EXTENSION_ATTRIBUTE));
    }
}
