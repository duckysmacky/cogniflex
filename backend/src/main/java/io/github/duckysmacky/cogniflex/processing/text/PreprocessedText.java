package io.github.duckysmacky.cogniflex.processing.text;

import java.util.List;

public record PreprocessedText(
    String normalizedText,
    String modelInput,
    List<HiddenCharacter> hiddenCharacters,
    NormalizationStats stats
) { }
