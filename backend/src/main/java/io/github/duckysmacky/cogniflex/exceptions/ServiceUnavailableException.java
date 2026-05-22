package io.github.duckysmacky.cogniflex.exceptions;

import io.github.duckysmacky.cogniflex.dto.ErrorResponse;
import org.springframework.http.ResponseEntity;

public class ServiceUnavailableException extends RuntimeException {
    public ServiceUnavailableException(String message) {
        super(message);
    }
}
