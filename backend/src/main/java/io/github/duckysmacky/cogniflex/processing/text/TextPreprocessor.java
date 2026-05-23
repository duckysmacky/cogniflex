package io.github.duckysmacky.cogniflex.processing.text;

import io.github.duckysmacky.cogniflex.exceptions.TextPreprocessingException;
import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;

@Component
public class TextPreprocessor {
    private final ModelInputPreparer modelInputPreparer;

    public TextPreprocessor(ModelInputPreparer modelInputPreparer) {
        this.modelInputPreparer = modelInputPreparer;
    }

    public PreprocessedText preprocess(String text) {
        return preprocess(text, TextPreprocessingOptions.defaults());
    }

    public PreprocessedText preprocess(String text, TextPreprocessingOptions options) {
        if (text == null) {
            throw TextPreprocessingException.missingText();
        }

        TextPreprocessingOptions effectiveOptions = options == null
            ? TextPreprocessingOptions.defaults()
            : options;

        List<HiddenCharacter> hiddenCharacters = scanHiddenCharacters(text);
        LineEndingNormalization lineEndingNormalization = normalizeLineEnding(text);
        String unicodeNormalized = Normalizer.normalize(
            lineEndingNormalization.text(),
            effectiveOptions.unicodeNormalizationForm()
        );
        WhitespaceNormalization whitespaceNormalization = normalizeWhitespaces(unicodeNormalized);
        String normalizedText = whitespaceNormalization.text().trim();

        if (normalizedText.isEmpty()) {
            throw TextPreprocessingException.emptyText();
        }

        ModelInput modelInput = modelInputPreparer.prepare(normalizedText, effectiveOptions);
        NormalizationStats stats = NormalizationStats.from(
            text,
            normalizedText,
            modelInput,
            lineEndingNormalization,
            !lineEndingNormalization.text().equals(unicodeNormalized),
            whitespaceNormalization,
            hiddenCharacters
        );

        return new PreprocessedText(
            normalizedText,
            modelInput.text(),
            hiddenCharacters,
            stats
        );
    }

    private List<HiddenCharacter> scanHiddenCharacters(String text) {
        if (text == null || text.isEmpty()) {
            return List.of();
        }

        List<HiddenCharacter> characters = new ArrayList<>();
        int charIndex = 0;
        int codePointIndex = 0;

        while (charIndex < text.length()) {
            int codePoint = text.codePointAt(charIndex);
            HiddenCharacter.Kind kind = HiddenCharacter.Kind.fromCodePoint(codePoint);

            if (kind != null) {
                characters.add(new HiddenCharacter(
                    codePoint,
                    charIndex,
                    codePointIndex,
                    kind,
                    printableCodePoint(codePoint)
                ));
            }

            charIndex += Character.charCount(codePoint);
            codePointIndex++;
        }

        return List.copyOf(characters);
    }

    private String printableCodePoint(int codePoint) {
        return "U+" + String.format("%04X", codePoint);
    }

    private LineEndingNormalization normalizeLineEnding(String text) {
        if (text == null || text.isEmpty()) {
            return new LineEndingNormalization("", 0);
        }

        StringBuilder builder = new StringBuilder(text.length());
        int normalizedLineEndings = 0;

        for (int index = 0; index < text.length(); index++) {
            char current = text.charAt(index);

            if (current == '\r') {
                if (index + 1 < text.length() && text.charAt(index + 1) == '\n') {
                    index++;
                }
                builder.append('\n');
                normalizedLineEndings++;
            } else if (current == '\u0085' || current == '\u2028' || current == '\u2029') {
                builder.append('\n');
                normalizedLineEndings++;
            } else {
                builder.append(current);
            }
        }

        return new LineEndingNormalization(builder.toString(), normalizedLineEndings);
    }

    private WhitespaceNormalization normalizeWhitespaces(String text) {
        StringBuilder builder = new StringBuilder(text.length());
        boolean pendingHorizontalSpace = false;
        int collapsedRuns = 0;
        int removedControls = 0;
        int convertedWhitespace = 0;

        for (int index = 0; index < text.length(); ) {
            int codePoint = text.codePointAt(index);
            index += Character.charCount(codePoint);

            if (codePoint == '\n') {
                if (!builder.isEmpty() && builder.charAt(builder.length() - 1) == ' ') {
                    builder.setLength(builder.length() - 1);
                }
                builder.append('\n');
                pendingHorizontalSpace = false;
                continue;
            }

            if (isHorizontalWhitespace(codePoint)) {
                if (codePoint != ' ') {
                    convertedWhitespace++;
                }
                if (!pendingHorizontalSpace && !builder.isEmpty() && builder.charAt(builder.length() - 1) != '\n') {
                    builder.append(' ');
                } else if (pendingHorizontalSpace) {
                    collapsedRuns++;
                }
                pendingHorizontalSpace = true;
                continue;
            }

            if (HiddenCharacter.Kind.isUnsafeControl(codePoint)) {
                removedControls++;
                continue;
            }

            builder.appendCodePoint(codePoint);
            pendingHorizontalSpace = false;
        }

        return new WhitespaceNormalization(
            builder.toString(),
            collapsedRuns,
            removedControls,
            convertedWhitespace
        );
    }

    private boolean isHorizontalWhitespace(int codePoint) {
        return codePoint != '\n'
            && (Character.isWhitespace(codePoint)
            || Character.isSpaceChar(codePoint)
            || codePoint == '\u00A0'
            || codePoint == '\u2007'
            || codePoint == '\u202F');
    }
}
