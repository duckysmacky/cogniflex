package io.github.duckysmacky.cogniflex.analysis;

import io.github.duckysmacky.cogniflex.processing.media.Dimensions;
import io.github.duckysmacky.cogniflex.processing.media.MediaFormat;
import io.github.duckysmacky.cogniflex.processing.media.ParsedMedia;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class ContentItemFactoryTest {
    private final ContentItemFactory factory = new ContentItemFactory();

    @Test
    void mediaItemCarriesDetectedFormatAndDimensionsForStaticAnalysis() {
        ParsedMedia media = new ParsedMedia(
            MediaType.IMAGE, new byte[] {1}, "a.png", "image/png", 1, "png",
            MediaFormat.PNG, new Dimensions(640, 480)
        );

        ContentItem item = factory.fromMedia(media);

        assertEquals("png", item.attributes().get(ContentItemFactory.FORMAT_ATTRIBUTE));
        assertEquals("640", item.attributes().get(ContentItemFactory.WIDTH_ATTRIBUTE));
        assertEquals("480", item.attributes().get(ContentItemFactory.HEIGHT_ATTRIBUTE));
        assertEquals("image/png", item.attributes().get(ContentItemFactory.CONTENT_TYPE_ATTRIBUTE));
    }

    @Test
    void absentFormatAndDimensionsAreNotAddedAsAttributes() {
        ParsedMedia media = new ParsedMedia(
            MediaType.VIDEO, new byte[] {1}, "a.mp4", "video/mp4", 1, "mp4", null, null
        );

        ContentItem item = factory.fromMedia(media);

        assertFalse(item.attributes().containsKey(ContentItemFactory.FORMAT_ATTRIBUTE));
        assertFalse(item.attributes().containsKey(ContentItemFactory.WIDTH_ATTRIBUTE));
        assertFalse(item.attributes().containsKey(ContentItemFactory.HEIGHT_ATTRIBUTE));
    }
}
