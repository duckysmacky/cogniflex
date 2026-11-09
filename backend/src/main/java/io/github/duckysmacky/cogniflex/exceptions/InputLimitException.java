package io.github.duckysmacky.cogniflex.exceptions;

import org.springframework.http.HttpStatus;

public class InputLimitException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    private InputLimitException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static InputLimitException textTooShort(int words, int minWords) {
        return new InputLimitException(
            HttpStatus.UNPROCESSABLE_CONTENT,
            "TEXT_TOO_SHORT",
            "Text has %d words, at least %d are required for a reliable result".formatted(words, minWords)
        );
    }

    public static InputLimitException textTooLong(int characters, int maxCharacters) {
        return new InputLimitException(
            HttpStatus.CONTENT_TOO_LARGE,
            "TEXT_TOO_LONG",
            "Text has %d characters, at most %d are allowed".formatted(characters, maxCharacters)
        );
    }

    public static InputLimitException fileTooLarge(String mediaKind, long bytes, long maxBytes) {
        return new InputLimitException(
            HttpStatus.CONTENT_TOO_LARGE,
            "FILE_TOO_LARGE",
            "%s is %d bytes, at most %d bytes are allowed".formatted(mediaKind, bytes, maxBytes)
        );
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
