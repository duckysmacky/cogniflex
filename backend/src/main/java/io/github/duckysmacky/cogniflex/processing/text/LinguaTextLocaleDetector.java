package io.github.duckysmacky.cogniflex.processing.text;

import com.github.pemistahl.lingua.api.Language;
import com.github.pemistahl.lingua.api.LanguageDetector;
import com.github.pemistahl.lingua.api.LanguageDetectorBuilder;
import org.springframework.stereotype.Component;

import java.util.Locale;

@Component
public class LinguaTextLocaleDetector implements TextLocaleDetector {
    private static final double MINIMUM_RELATIVE_DISTANCE = 0.15;

    private final LanguageDetector detector;

    public LinguaTextLocaleDetector() {
        this(LanguageDetectorBuilder.fromAllSpokenLanguages()
            .withLowAccuracyMode()
            .withMinimumRelativeDistance(MINIMUM_RELATIVE_DISTANCE)
            .build());
    }

    LinguaTextLocaleDetector(LanguageDetector detector) {
        this.detector = detector;
    }

    @Override
    public DetectedTextLocale detect(String text) {
        if (text == null || text.isBlank()) {
            return DetectedTextLocale.unknown();
        }

        Language language = detector.detectLanguageOf(text);
        if (language == Language.UNKNOWN) {
            return DetectedTextLocale.unknown();
        }

        return new DetectedTextLocale(toLocale(language));
    }

    private Locale toLocale(Language language) {
        String tag = language.getIsoCode639_1().name().toLowerCase(Locale.ROOT);
        Locale locale = Locale.forLanguageTag(tag);
        return locale.getLanguage().isBlank() ? Locale.ROOT : locale;
    }
}
