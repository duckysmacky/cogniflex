package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.exceptions.MediaParsingException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaParserTest {
    private final MediaParser parser = new MediaParser(new MediaTypeResolver());

    @Test
    void parseReturnsImageBytesTypeAndMetadata() {
        byte[] bytes = new byte[] {1, 2, 3};
        MockMultipartFile file = new MockMultipartFile(
            "file",
            " sample.PNG ",
            "image/png",
            bytes
        );

        ParsedMedia media = parser.parse(file);

        assertEquals(MediaType.IMAGE, media.mediaType());
        assertArrayEquals(bytes, media.bytes());
        assertEquals("sample.PNG", media.filename());
        assertEquals("image/png", media.contentType());
        assertEquals(3, media.size());
        assertEquals("png", media.extension());
    }

    @Test
    void parseReturnsVideoType() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "clip.mp4",
            "video/mp4",
            new byte[] {4, 5}
        );

        ParsedMedia media = parser.parse(file);

        assertEquals(MediaType.VIDEO, media.mediaType());
        assertEquals("mp4", media.extension());
    }

    @Test
    void parseAllowsMissingFilenameMetadata() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            null,
            "image/jpeg",
            new byte[] {1}
        );

        ParsedMedia media = parser.parse(file);

        assertNull(media.filename());
        assertNull(media.extension());
    }

    @Test
    void parseRejectsMissingFile() {
        MediaParsingException exception = assertThrows(MediaParsingException.class, () -> parser.parse(null));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void parseRejectsUnsupportedMediaType() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "document.pdf",
            "application/pdf",
            new byte[] {1}
        );

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> parser.parse(file));

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, exception.getStatusCode());
    }
}
