package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.config.InputLimitsProperties;
import io.github.duckysmacky.cogniflex.exceptions.InputLimitException;
import io.github.duckysmacky.cogniflex.exceptions.InvalidMediaException;
import io.github.duckysmacky.cogniflex.processing.media.Dimensions;
import io.github.duckysmacky.cogniflex.processing.media.MediaFormat;
import io.github.duckysmacky.cogniflex.processing.media.MediaInfo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.util.unit.DataSize;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaContentValidatorTest {
    private final MediaContentValidator validator = new MediaContentValidator(
        new InputLimitsProperties(
            new InputLimitsProperties.Text(3, 20),
            new InputLimitsProperties.Media(DataSize.ofMegabytes(10), DataSize.ofMegabytes(100), 100L)
        )
    );

    @Test
    void unrecognisedContentIsRejectedAs415() {
        InvalidMediaException ex = assertThrows(
            InvalidMediaException.class,
            () -> validator.validate(new MediaInfo(MediaType.IMAGE, null, null))
        );

        assertEquals("UNSUPPORTED_MEDIA_TYPE", ex.code());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.status());
    }

    @Test
    void contentTypeThatContradictsTheBytesIsRejectedAs415() {
        InvalidMediaException ex = assertThrows(
            InvalidMediaException.class,
            () -> validator.validate(new MediaInfo(MediaType.VIDEO, MediaFormat.PNG, new Dimensions(1, 1)))
        );

        assertEquals("MEDIA_TYPE_MISMATCH", ex.code());
        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, ex.status());
    }

    @Test
    void imageWithUnreadableHeaderIsRejectedAs400() {
        InvalidMediaException ex = assertThrows(
            InvalidMediaException.class,
            () -> validator.validate(new MediaInfo(MediaType.IMAGE, MediaFormat.PNG, null))
        );

        assertEquals("INVALID_IMAGE", ex.code());
        assertEquals(HttpStatus.BAD_REQUEST, ex.status());
    }

    @Test
    void imageOverThePixelLimitIsRejectedAs413() {
        InputLimitException ex = assertThrows(
            InputLimitException.class,
            () -> validator.validate(new MediaInfo(MediaType.IMAGE, MediaFormat.PNG, new Dimensions(101, 1)))
        );

        assertEquals("IMAGE_TOO_LARGE", ex.code());
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, ex.status());
    }

    @Test
    void pixelCountDoesNotOverflowInt() {
        assertThrows(
            InputLimitException.class,
            () -> validator.validate(new MediaInfo(MediaType.IMAGE, MediaFormat.PNG, new Dimensions(100_000, 100_000)))
        );
    }

    @Test
    void imageExactlyAtThePixelLimitPasses() {
        assertDoesNotThrow(
            () -> validator.validate(new MediaInfo(MediaType.IMAGE, MediaFormat.WEBP, new Dimensions(10, 10)))
        );
    }

    @Test
    void videoSkipsThePixelCheck() {
        assertDoesNotThrow(() -> validator.validate(new MediaInfo(MediaType.VIDEO, MediaFormat.MP4, null)));
    }
}
