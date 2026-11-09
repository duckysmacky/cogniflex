package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.exceptions.MediaParsingException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.MemoryCacheImageInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * Inspects an upload and reports what it is. Only describes: unrecognised bytes or an unreadable image header
 * give {@code null} fields in {@link MediaInfo}; rejecting them is the job of the content validator.
 * Never decodes pixels, only the leading bytes and the image header are read.
 */
@Component
public class MediaTypeResolver {
    static {
        // Picks up ImageIO plugins (WebP) from nested jars of the executable Spring Boot jar
        ImageIO.scanForPlugins();
    }

    public MediaInfo resolve(MultipartFile file) {
        MediaType declaredType = declaredTypeOf(file);
        MediaFormat format = MediaFormat.detect(readHead(file));
        Dimensions dimensions = format != null && format.mediaType() == MediaType.IMAGE
            ? readDimensions(file)
            : null;

        return new MediaInfo(declaredType, format, dimensions);
    }

    private MediaType declaredTypeOf(MultipartFile file) {
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

    private byte[] readHead(MultipartFile file) {
        try (InputStream in = file.getInputStream()) {
            return in.readNBytes(MediaFormat.SIGNATURE_LENGTH);
        } catch (IOException ex) {
            throw MediaParsingException.unreadableFile(ex);
        }
    }

    private Dimensions readDimensions(MultipartFile file) {
        try (InputStream in = file.getInputStream();
             ImageInputStream stream = new MemoryCacheImageInputStream(in)) {
            Iterator<ImageReader> readers = ImageIO.getImageReaders(stream);
            if (!readers.hasNext()) {
                return null;
            }

            ImageReader reader = readers.next();
            try {
                reader.setInput(stream, true, true);
                return new Dimensions(reader.getWidth(0), reader.getHeight(0));
            } finally {
                reader.dispose();
            }
        } catch (IOException | RuntimeException ex) {
            // Truncated or corrupt header: reported as "no dimensions", the validator rejects the image
            return null;
        }
    }
}
