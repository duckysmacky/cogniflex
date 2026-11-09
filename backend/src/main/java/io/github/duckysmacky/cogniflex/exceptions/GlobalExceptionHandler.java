package io.github.duckysmacky.cogniflex.exceptions;

import io.github.duckysmacky.cogniflex.dto.ErrorResponse;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(InputLimitException.class)
    public ResponseEntity<ErrorResponse> handleInputLimitException(InputLimitException ex) {
        return ResponseEntity.status(ex.status())
            .body(new ErrorResponse(ex.code(), ex.getMessage()));
    }

    @ExceptionHandler(InvalidMediaException.class)
    public ResponseEntity<ErrorResponse> handleInvalidMediaException(InvalidMediaException ex) {
        return ResponseEntity.status(ex.status())
            .body(new ErrorResponse(ex.code(), ex.getMessage()));
    }

    // Must stay more specific than the MultipartException handler below (it is a subclass)
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErrorResponse> handleMaxUploadSizeExceeded(MaxUploadSizeExceededException ex) {
        return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
            .body(new ErrorResponse("FILE_TOO_LARGE", "Upload exceeds the maximum allowed request size"));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof FileUploadException) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                    "File upload failed",
                    "Upload is incomplete or unreadable"
                ));
        }
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                "Multipart request error",
                "Invalid multipart request"
            ));
    }

    @ExceptionHandler(UnsupportedAnalysisException.class)
    public ResponseEntity<ErrorResponse> handleUnsupportedAnalysisException(UnsupportedAnalysisException ex) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED)
            .body(new ErrorResponse(
                "UNSUPPORTED",
                ex.getMessage()
            ));
    }

    @ExceptionHandler(ServiceUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleServiceUnavailableException(ServiceUnavailableException ex) {
        return ResponseEntity.status(503)
            .body(new ErrorResponse(
                "Service unavailable",
                ex.getMessage()
            ));
    }
}