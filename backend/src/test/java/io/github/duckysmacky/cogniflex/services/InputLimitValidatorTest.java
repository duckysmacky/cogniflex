package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.config.InputLimitsProperties;
import io.github.duckysmacky.cogniflex.exceptions.InputLimitException;
import io.github.duckysmacky.cogniflex.processing.text.PreprocessedText;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class InputLimitValidatorTest {
    private final InputLimitValidator validator = new InputLimitValidator(
        new InputLimitsProperties(
            new InputLimitsProperties.Text(3, 20),
            new InputLimitsProperties.Media(DataSize.ofBytes(10), DataSize.ofBytes(100))
        ),
        new WordTokenizer()
    );

    @Test
    void textAtMaxLengthPasses() {
        assertDoesNotThrow(() -> validator.validateTextLength("a".repeat(20)));
    }

    @Test
    void textOverMaxLengthIsRejectedAs413() {
        InputLimitException ex = assertThrows(
            InputLimitException.class,
            () -> validator.validateTextLength("a".repeat(21))
        );

        assertEquals("TEXT_TOO_LONG", ex.code());
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, ex.status());
    }

    @Test
    void textWithMinWordsPasses() {
        assertDoesNotThrow(() -> validator.validateWordCount(preprocessed("one two, three!")));
    }

    @Test
    void textBelowMinWordsIsRejectedAs422() {
        InputLimitException ex = assertThrows(
            InputLimitException.class,
            () -> validator.validateWordCount(preprocessed("only two... ---"))
        );

        assertEquals("TEXT_TOO_SHORT", ex.code());
        assertEquals(HttpStatus.UNPROCESSABLE_CONTENT, ex.status());
    }

    @Test
    void imageAndVideoUseTheirOwnLimits() {
        MockMultipartFile fifty = new MockMultipartFile("file", "f", "image/png", new byte[50]);

        InputLimitException ex = assertThrows(
            InputLimitException.class,
            () -> validator.validateMediaSize(MediaType.IMAGE, fifty)
        );
        assertEquals("FILE_TOO_LARGE", ex.code());
        assertEquals(HttpStatus.CONTENT_TOO_LARGE, ex.status());

        assertDoesNotThrow(() -> validator.validateMediaSize(MediaType.VIDEO, fifty));
    }

    @Test
    void mediaAtExactLimitPasses() {
        MockMultipartFile exact = new MockMultipartFile("file", "f", "image/png", new byte[10]);

        assertDoesNotThrow(() -> validator.validateMediaSize(MediaType.IMAGE, exact));
    }

    private PreprocessedText preprocessed(String text) {
        return new PreprocessedText(text, text, List.of(), null, Locale.ENGLISH);
    }
}
