package io.github.duckysmacky.cogniflex.processing.text;

public record LineEndingNormalization(
    String text,
    int normalizedLineEndings
) { }
