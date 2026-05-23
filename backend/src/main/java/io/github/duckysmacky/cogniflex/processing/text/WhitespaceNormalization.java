package io.github.duckysmacky.cogniflex.processing.text;

public record WhitespaceNormalization(
    String text,
    int collapsedHorizontalWhitespaceRuns,
    int removedControlCharacters,
    int convertedWhitespaceCharacters
) { }
