package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.config.InputLimitsProperties;
import io.github.duckysmacky.cogniflex.exceptions.InputLimitException;
import io.github.duckysmacky.cogniflex.processing.text.PreprocessedText;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.unit.DataSize;
import org.springframework.web.multipart.MultipartFile;

@Component
@EnableConfigurationProperties(InputLimitsProperties.class)
public class InputLimitValidator {
    private final InputLimitsProperties limits;
    private final WordTokenizer wordTokenizer;

    public InputLimitValidator(InputLimitsProperties limits, WordTokenizer wordTokenizer) {
        this.limits = limits;
        this.wordTokenizer = wordTokenizer;
    }

    /** Cheap check on the raw text, so oversized input is never normalized. */
    public void validateTextLength(String rawText) {
        if (rawText != null && rawText.length() > limits.text().maxCharacters()) {
            throw InputLimitException.textTooLong(rawText.length(), limits.text().maxCharacters());
        }
    }

    public void validateWordCount(PreprocessedText text) {
        int words = wordTokenizer.tokenize(text.normalizedText(), text.locale()).size();
        if (words < limits.text().minWords()) {
            throw InputLimitException.textTooShort(words, limits.text().minWords());
        }
    }

    public void validateMediaSize(MediaType mediaType, MultipartFile file) {
        DataSize max = switch (mediaType) {
            case IMAGE -> limits.media().maxImageSize();
            case VIDEO -> limits.media().maxVideoSize();
        };

        if (file.getSize() > max.toBytes()) {
            throw InputLimitException.fileTooLarge(mediaType.name().toLowerCase(), file.getSize(), max.toBytes());
        }
    }
}
