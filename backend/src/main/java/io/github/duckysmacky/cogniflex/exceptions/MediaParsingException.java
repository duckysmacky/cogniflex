package io.github.duckysmacky.cogniflex.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class MediaParsingException extends ResponseStatusException {
    private MediaParsingException(HttpStatus status, String reason) {
        super(status, reason);
    }

    private MediaParsingException(HttpStatus status, String reason, Throwable cause) {
        super(status, reason, cause);
    }

    public static MediaParsingException missingFile() {
        return new MediaParsingException(HttpStatus.BAD_REQUEST, "File is required");
    }

    public static MediaParsingException emptyFile() {
        return new MediaParsingException(HttpStatus.BAD_REQUEST, "File is empty");
    }

    public static MediaParsingException unreadableFile(Throwable cause) {
        return new MediaParsingException(HttpStatus.BAD_REQUEST, "Cannot read uploaded file", cause);
    }
}
