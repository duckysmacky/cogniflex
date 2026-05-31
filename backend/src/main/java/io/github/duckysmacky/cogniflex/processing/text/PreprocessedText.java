package io.github.duckysmacky.cogniflex.processing.text;

import java.util.List;
import java.util.Locale;

public record PreprocessedText(
    String normalizedText,
    String modelInput,
    List<HiddenCharacter> hiddenCharacters,
    NormalizationStats stats,
    Locale locale
) {
    public PreprocessedText {
        hiddenCharacters = hiddenCharacters == null ? List.of() : List.copyOf(hiddenCharacters);
        locale = locale == null ? Locale.ROOT : locale;
    }
}
