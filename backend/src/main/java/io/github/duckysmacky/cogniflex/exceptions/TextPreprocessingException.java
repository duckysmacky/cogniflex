package io.github.duckysmacky.cogniflex.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class TextPreprocessingException extends ResponseStatusException {
    private TextPreprocessingException(String reason) {
        super(HttpStatus.BAD_REQUEST, reason);
    }

    public static TextPreprocessingException missingText() {
        return new TextPreprocessingException("Text is required");
    }

    public static TextPreprocessingException emptyText() {
        return new TextPreprocessingException("Text is empty after normalization");
    }
}
