package io.github.duckysmacky.cogniflex.processing;

import io.github.duckysmacky.cogniflex.exceptions.TextPreprocessingException;
import io.github.duckysmacky.cogniflex.processing.text.*;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TextPreprocessorTest {
    private final TextPreprocessor preprocessor = new TextPreprocessor(
        new ModelInputPreparer(),
        new HiddenCharacterScanner(),
        text -> DetectedTextLocale.english()
    );

    @Test
    void preprocessNormalizesUnicodeLineEndingsAndWhitespace() {
        PreprocessedText result = preprocessor.preprocess(" cafe\u0301\r\n\tfoo\u00A0\u00A0bar \u2028 baz ");

        assertEquals("café\nfoo bar\nbaz", result.normalizedText());
        assertEquals(result.normalizedText(), result.modelInput());
        assertEquals(Locale.ENGLISH, result.locale());
        assertTrue(result.stats().unicodeNormalizationChangedText());
        assertEquals(2, result.stats().lineEndingNormalizations());
        assertTrue(result.stats().convertedWhitespaceCharacters() >= 2);
    }

    @Test
    void preprocessDiscoversHiddenCharactersButRemovesUnsafeOnesFromModelInput() {
        PreprocessedText result = preprocessor.preprocess("a\u200Bb\u202Ec\u00A0d");

        assertEquals("a\u200Bb\u202Ec d", result.normalizedText());
        assertEquals("abc d", result.modelInput());
        assertEquals(3, result.hiddenCharacters().size());
        assertEquals(3, result.stats().hiddenCharacters());
        assertTrue(result.hiddenCharacters().stream()
            .anyMatch(character -> character.kind() == HiddenCharacter.Kind.ZERO_WIDTH));
        assertTrue(result.hiddenCharacters().stream()
            .anyMatch(character -> character.kind() == HiddenCharacter.Kind.BIDI_CONTROL));
        assertTrue(result.hiddenCharacters().stream()
            .anyMatch(character -> character.kind() == HiddenCharacter.Kind.NON_BREAKING_SPACE));
    }

    @Test
    void preprocessCanLimitModelInputWithoutChangingNormalizedText() {
        PreprocessedText result = preprocessor.preprocess(
            "abcdef",
            TextPreprocessingOptions.forModelInput().withMaxModelInputCharacters(3)
        );

        assertEquals("abcdef", result.normalizedText());
        assertEquals("abc", result.modelInput());
        assertTrue(result.stats().modelInputTruncated());
    }

    @Test
    void normalizeTextRejectsBlankInputAfterNormalization() {
        assertThrows(TextPreprocessingException.class, () -> preprocessor.preprocess(" \r\n\t "));
    }

    @Test
    void hiddenCharactersResultIsImmutable() {
        PreprocessedText result = preprocessor.preprocess("a\u200Bb");

        assertFalse(result.hiddenCharacters().isEmpty());
        assertThrows(UnsupportedOperationException.class, () -> result.hiddenCharacters().clear());
    }
}
