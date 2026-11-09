package io.github.duckysmacky.cogniflex.exceptions;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import org.springframework.http.HttpStatus;

public class InvalidMediaException extends RuntimeException {
    private final HttpStatus status;
    private final String code;

    private InvalidMediaException(HttpStatus status, String code, String message) {
        super(message);
        this.status = status;
        this.code = code;
    }

    public static InvalidMediaException unsupportedFormat() {
        return new InvalidMediaException(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "UNSUPPORTED_MEDIA_TYPE",
            "File content is not a supported image or video format"
        );
    }

    public static InvalidMediaException typeMismatch(MediaType declared, MediaType actual) {
        return new InvalidMediaException(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "MEDIA_TYPE_MISMATCH",
            "Content-Type declares %s but the file content is %s"
                .formatted(declared.name().toLowerCase(), actual.name().toLowerCase())
        );
    }

    public static InvalidMediaException invalidImage() {
        return new InvalidMediaException(
            HttpStatus.BAD_REQUEST,
            "INVALID_IMAGE",
            "Image header is corrupt or unreadable"
        );
    }

    public HttpStatus status() {
        return status;
    }

    public String code() {
        return code;
    }
}
