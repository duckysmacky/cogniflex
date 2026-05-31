package io.github.duckysmacky.cogniflex.processing.text;

import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LinguaTextLocaleDetectorTest {
    private final LinguaTextLocaleDetector detector = new LinguaTextLocaleDetector();

    @Test
    void detectsEnglishText() {
        DetectedTextLocale result = detector.detect("""
            This service analyzes written content before it is sent to the machine learning model.
            The language detector should recognize that this paragraph is English and keep dynamic
            text analysis enabled for the request.
            """);

        assertEquals(Locale.ENGLISH.getLanguage(), result.locale().getLanguage());
    }

    @Test
    void detectsNonEnglishText() {
        DetectedTextLocale result = detector.detect("""
            Этот сервис анализирует письменный текст перед отправкой в модель машинного обучения.
            Детектор языка должен распознать, что этот абзац написан на русском языке, и отключить
            динамический анализ текста для такого запроса.
            """);

        assertEquals("ru", result.locale().getLanguage());
    }

    @Test
    void returnsRootLocaleForAmbiguousText() {
        DetectedTextLocale result = detector.detect("prologue");

        assertEquals(Locale.ROOT, result.locale());
    }
}
