package io.github.duckysmacky.cogniflex.exceptions;

import io.github.duckysmacky.cogniflex.dto.ErrorResponse;
import org.apache.tomcat.util.http.fileupload.FileUploadException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ErrorResponse> handleMultipartException(MultipartException ex) {
        Throwable cause = ex.getCause();

        if (cause instanceof FileUploadException) {
            return ResponseEntity.badRequest()
                .body(new ErrorResponse(
                    "File upload failed",
                    "Upload is incomplete or file is too large (> 1024MB)"
                ));
        }
        return ResponseEntity.badRequest()
            .body(new ErrorResponse(
                "Multipart request error",
                "Invalid multipart request"
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