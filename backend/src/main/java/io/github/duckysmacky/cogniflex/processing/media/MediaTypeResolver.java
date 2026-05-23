package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@Component
public class MediaTypeResolver {
    public MediaType resolve(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String contentType = file.getContentType();

        if (contentType == null || contentType.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content type is missing");
        }

        if (contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        }

        if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }

        throw new ResponseStatusException(
            HttpStatus.UNSUPPORTED_MEDIA_TYPE,
            "Only image and video files are supported"
        );
    }
}