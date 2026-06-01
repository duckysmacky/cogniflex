package io.github.duckysmacky.cogniflex.processing.text;

import org.springframework.stereotype.Component;

@Component
public class ModelInputPreparer {
    public ModelInput prepare(String normalizedText, TextPreprocessingOptions options) {
        String modelInput = normalizedText;

        if (options.stripUnsafeHiddenCharactersFromModelInput()) {
            modelInput = stripModelUnsafeHiddenCharacters(modelInput);
        }

        modelInput = collapseExcessBlankLines(modelInput, options.maxConsecutiveBlankLinesInModelInput()).trim();

        if (options.maxModelInputCharacters() >= 0 && modelInput.length() > options.maxModelInputCharacters()) {
            return new ModelInput(modelInput.substring(0, options.maxModelInputCharacters()).trim(), true);
        }

        return new ModelInput(modelInput, false);
    }

    private String stripModelUnsafeHiddenCharacters(String text) {
        StringBuilder builder = new StringBuilder(text.length());

        for (int index = 0; index < text.length(); ) {
            int codePoint = text.codePointAt(index);
            index += Character.charCount(codePoint);
            HiddenCharacter.Kind kind = HiddenCharacter.Kind.fromCodePoint(codePoint);

            if (kind == null || !kind.unsafeForModelInput()) {
                builder.appendCodePoint(codePoint);
            }
        }

        return builder.toString();
    }

    private String collapseExcessBlankLines(String text, int maxConsecutiveBlankLines) {
        StringBuilder builder = new StringBuilder(text.length());
        int consecutiveNewlines = 0;

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);
            if (current == '\n') {
                consecutiveNewlines++;
                if (consecutiveNewlines <= maxConsecutiveBlankLines) {
                    builder.append(current);
                }
            } else {
                consecutiveNewlines = 0;
                builder.append(current);
            }
        }

        return builder.toString();
    }
}
