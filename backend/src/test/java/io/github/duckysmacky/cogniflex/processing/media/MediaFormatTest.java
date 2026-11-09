package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MediaFormatTest {
    @Test
    void detectsImageSignatures() {
        assertEquals(MediaFormat.PNG, MediaFormat.detect(bytes(0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0)));
        assertEquals(MediaFormat.JPEG, MediaFormat.detect(bytes(0xFF, 0xD8, 0xFF, 0xE0, 0, 0x10)));
        assertEquals(MediaFormat.GIF, MediaFormat.detect(ascii("GIF89a....")));
        assertEquals(MediaFormat.GIF, MediaFormat.detect(ascii("GIF87a....")));
        assertEquals(MediaFormat.BMP, MediaFormat.detect(ascii("BM......")));
        assertEquals(MediaFormat.WEBP, MediaFormat.detect(ascii("RIFF\0\0\0\0WEBPVP8L")));
    }

    @Test
    void detectsVideoSignatures() {
        assertEquals(MediaFormat.MP4, MediaFormat.detect(ascii("\0\0\0\u0018ftypmp42")));
        assertEquals(MediaFormat.WEBM, MediaFormat.detect(bytes(0x1A, 0x45, 0xDF, 0xA3, 0x01)));
        assertEquals(MediaFormat.AVI, MediaFormat.detect(ascii("RIFF\0\0\0\0AVI LIST")));
    }

    @Test
    void riffContainerIsToldApartByItsFormTag() {
        assertNull(MediaFormat.detect(ascii("RIFF\0\0\0\0WAVEfmt ")));
    }

    @Test
    void unknownOrTooShortHeadIsNotRecognised() {
        assertNull(MediaFormat.detect(ascii("hello, this is plain text")));
        assertNull(MediaFormat.detect(bytes(0x89, 'P')));
        assertNull(MediaFormat.detect(new byte[0]));
        assertNull(MediaFormat.detect(null));
    }

    @Test
    void formatsKnowTheirMediaKind() {
        assertEquals(MediaType.IMAGE, MediaFormat.WEBP.mediaType());
        assertEquals(MediaType.VIDEO, MediaFormat.AVI.mediaType());
        assertEquals("image/webp", MediaFormat.WEBP.mimeType());
    }

    private static byte[] bytes(int... values) {
        byte[] result = new byte[values.length];
        for (int index = 0; index < values.length; index++) {
            result[index] = (byte) values[index];
        }
        return result;
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.ISO_8859_1);
    }
}
