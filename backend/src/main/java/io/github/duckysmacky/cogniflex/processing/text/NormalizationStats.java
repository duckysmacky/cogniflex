package io.github.duckysmacky.cogniflex.processing.text;

import java.util.List;

public record NormalizationStats(
    int originalCharacters,
    int normalizedCharacters,
    int modelInputCharacters,
    int originalCodePoints,
    int normalizedCodePoints,
    int lineEndingNormalizations,
    boolean unicodeNormalizationChangedText,
    int collapsedHorizontalWhitespaceRuns,
    int removedControlCharacters,
    int convertedWhitespaceCharacters,
    int hiddenCharacters,
    boolean modelInputTruncated
) {
    public static NormalizationStats from(
        String originalText,
        String normalizedText,
        ModelInput modelInput,
        LineEndingNormalization lineEndingNormalization,
        boolean unicodeNormalizationChangedText,
        WhitespaceNormalization whitespaceNormalization,
        List<HiddenCharacter> hiddenCharacters
    ) {
        return new NormalizationStats(
            originalText.length(),
            normalizedText.length(),
            modelInput.text().length(),
            originalText.codePointCount(0, originalText.length()),
            normalizedText.codePointCount(0, normalizedText.length()),
            lineEndingNormalization.normalizedLineEndings(),
            unicodeNormalizationChangedText,
            whitespaceNormalization.collapsedHorizontalWhitespaceRuns(),
            whitespaceNormalization.removedControlCharacters(),
            whitespaceNormalization.convertedWhitespaceCharacters(),
            hiddenCharacters.size(),
            modelInput.truncated()
        );
    }
}
