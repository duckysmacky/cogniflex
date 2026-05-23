package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.exceptions.MediaParsingException;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Locale;

@Component
public class MediaParser {
    private final MediaTypeResolver mediaTypeResolver;

    public MediaParser(MediaTypeResolver mediaTypeResolver) {
        this.mediaTypeResolver = mediaTypeResolver;
    }

    public ParsedMedia parse(MultipartFile file) {
        if (file == null) {
            throw MediaParsingException.missingFile();
        }

        var mediaType = mediaTypeResolver.resolve(file);
        byte[] bytes = readBytes(file);

        if (bytes.length == 0) {
            throw MediaParsingException.emptyFile();
        }

        return new ParsedMedia(
            mediaType,
            bytes,
            normalizeFilename(file.getOriginalFilename()),
            file.getContentType(),
            bytes.length,
            extensionOf(file.getOriginalFilename())
        );
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw MediaParsingException.unreadableFile(ex);
        }
    }

    private String normalizeFilename(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }

        return filename.trim();
    }

    private String extensionOf(String filename) {
        String normalizedFilename = normalizeFilename(filename);
        if (normalizedFilename == null) {
            return null;
        }

        int lastSeparator = Math.max(
            normalizedFilename.lastIndexOf('/'),
            normalizedFilename.lastIndexOf('\\')
        );
        int lastDot = normalizedFilename.lastIndexOf('.');

        if (lastDot <= lastSeparator || lastDot == normalizedFilename.length() - 1) {
            return null;
        }

        return normalizedFilename.substring(lastDot + 1).toLowerCase(Locale.ROOT);
    }
}
