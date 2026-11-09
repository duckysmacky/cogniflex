package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.exceptions.MediaParsingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaParserTest {
    private final MediaParser parser = new MediaParser();
    private final MediaInfo imageInfo = new MediaInfo(MediaType.IMAGE, MediaFormat.PNG, new Dimensions(4, 3));

    @Test
    void parseReturnsImageBytesTypeAndMetadata() {
        byte[] bytes = new byte[] {1, 2, 3};
        MockMultipartFile file = new MockMultipartFile(
            "file",
            " sample.PNG ",
            "image/png",
            bytes
        );

        ParsedMedia media = parser.parse(file, imageInfo);

        assertEquals(MediaType.IMAGE, media.mediaType());
        assertArrayEquals(bytes, media.bytes());
        assertEquals("sample.PNG", media.filename());
        assertEquals("image/png", media.contentType());
        assertEquals(3, media.size());
        assertEquals("png", media.extension());
    }

    @Test
    void parseCarriesDetectedFormatAndDimensions() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", "image/png", new byte[] {1});

        ParsedMedia media = parser.parse(file, imageInfo);

        assertEquals(MediaFormat.PNG, media.format());
        assertEquals(new Dimensions(4, 3), media.dimensions());
    }

    @Test
    void parseReturnsVideoType() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "clip.mp4",
            "video/mp4",
            new byte[] {4, 5}
        );

        ParsedMedia media = parser.parse(file, new MediaInfo(MediaType.VIDEO, MediaFormat.MP4, null));

        assertEquals(MediaType.VIDEO, media.mediaType());
        assertEquals("mp4", media.extension());
        assertNull(media.dimensions());
    }

    @Test
    void parseAllowsMissingFilenameMetadata() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            null,
            "image/jpeg",
            new byte[] {1}
        );

        ParsedMedia media = parser.parse(file, imageInfo);

        assertNull(media.filename());
        assertNull(media.extension());
    }

    @Test
    void parseRejectsMissingFile() {
        MediaParsingException exception = assertThrows(
            MediaParsingException.class,
            () -> parser.parse(null, imageInfo)
        );

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }
}
