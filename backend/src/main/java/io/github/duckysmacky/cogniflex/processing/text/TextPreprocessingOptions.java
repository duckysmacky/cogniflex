package io.github.duckysmacky.cogniflex.processing.text;

import java.text.Normalizer;

public record TextPreprocessingOptions(
    Normalizer.Form unicodeNormalizationForm,
    int maxModelInputCharacters,
    boolean stripUnsafeHiddenCharactersFromModelInput,
    int maxConsecutiveBlankLinesInModelInput
) {
    public static final int NO_MODEL_INPUT_LIMIT = -1;

    public TextPreprocessingOptions {
        if (unicodeNormalizationForm == null) {
            unicodeNormalizationForm = Normalizer.Form.NFC;
        }
        if (maxConsecutiveBlankLinesInModelInput < 0) {
            throw new IllegalArgumentException("Max consecutive blank lines cannot be negative");
        }
    }

    public static TextPreprocessingOptions defaults() {
        return forModelInput();
    }

    public static TextPreprocessingOptions forModelInput() {
        return new TextPreprocessingOptions(
            Normalizer.Form.NFC,
            NO_MODEL_INPUT_LIMIT,
            true,
            2
        );
    }

    public TextPreprocessingOptions withMaxModelInputCharacters(int maxModelInputCharacters) {
        return new TextPreprocessingOptions(
            unicodeNormalizationForm,
            maxModelInputCharacters,
            stripUnsafeHiddenCharactersFromModelInput,
            maxConsecutiveBlankLinesInModelInput
        );
    }
}
