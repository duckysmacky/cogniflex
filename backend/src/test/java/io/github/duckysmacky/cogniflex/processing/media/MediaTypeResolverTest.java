package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.zip.CRC32;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MediaTypeResolverTest {
    // Smallest valid lossless WebP (1x1 pixel)
    private static final String TINY_WEBP_BASE64 = "UklGRhoAAABXRUJQVlA4TA0AAAAvAAAAEAcQERGIiP4HAA==";

    private final MediaTypeResolver resolver = new MediaTypeResolver();

    @Test
    void realPngIsRecognisedWithItsDimensions() throws IOException {
        byte[] bytes = new ClassPathResource("static/image_test.png").getInputStream().readAllBytes();

        MediaInfo info = resolver.resolve(new MockMultipartFile("file", "a.png", "image/png", bytes));

        assertEquals(MediaType.IMAGE, info.declaredType());
        assertEquals(MediaFormat.PNG, info.format());
        org.junit.jupiter.api.Assertions.assertNotNull(info.dimensions());
    }

    @Test
    void realMp4IsRecognisedWithoutDimensions() throws IOException {
        byte[] bytes = new ClassPathResource("static/video_test.mp4").getInputStream().readAllBytes();

        MediaInfo info = resolver.resolve(new MockMultipartFile("file", "a.mp4", "video/mp4", bytes));

        assertEquals(MediaType.VIDEO, info.declaredType());
        assertEquals(MediaFormat.MP4, info.format());
        assertNull(info.dimensions());
    }

    @Test
    void webpHeaderIsReadByTheImageIoPlugin() {
        byte[] bytes = Base64.getDecoder().decode(TINY_WEBP_BASE64);

        MediaInfo info = resolver.resolve(new MockMultipartFile("file", "a.webp", "image/webp", bytes));

        assertEquals(MediaFormat.WEBP, info.format());
        assertEquals(new Dimensions(1, 1), info.dimensions());
    }

    @Test
    void hugeDimensionsAreReadFromTheHeaderWithoutAnyPixelData() throws IOException {
        byte[] bytes = pngHeaderOnly(100_000, 100_000);

        MediaInfo info = resolver.resolve(new MockMultipartFile("file", "bomb.png", "image/png", bytes));

        assertEquals(MediaFormat.PNG, info.format());
        assertEquals(new Dimensions(100_000, 100_000), info.dimensions());
        assertEquals(10_000_000_000L, info.dimensions().pixels());
    }

    @Test
    void spoofedContentTypeIsReportedNotThrown() {
        MockMultipartFile text = new MockMultipartFile("file", "a.png", "image/png", "not an image".getBytes());

        MediaInfo info = resolver.resolve(text);

        assertEquals(MediaType.IMAGE, info.declaredType());
        assertNull(info.format());
        assertNull(info.dimensions());
    }

    @Test
    void truncatedImageHasFormatButNoDimensions() {
        byte[] truncated = {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A, 0, 0};

        MediaInfo info = resolver.resolve(new MockMultipartFile("file", "a.png", "image/png", truncated));

        assertEquals(MediaFormat.PNG, info.format());
        assertNull(info.dimensions());
    }

    @Test
    void mismatchBetweenDeclaredAndDetectedKindIsVisibleInInfo() throws IOException {
        byte[] png = new ClassPathResource("static/image_test.png").getInputStream().readAllBytes();

        MediaInfo info = resolver.resolve(new MockMultipartFile("file", "a.mp4", "video/mp4", png));

        assertEquals(MediaType.VIDEO, info.declaredType());
        assertEquals(MediaFormat.PNG, info.format());
    }

    @Test
    void emptyFileIsRejected() {
        MockMultipartFile empty = new MockMultipartFile("file", "a.png", "image/png", new byte[0]);

        assertEquals(HttpStatus.BAD_REQUEST, statusOf(empty));
    }

    @Test
    void missingContentTypeIsRejected() {
        MockMultipartFile file = new MockMultipartFile("file", "a.png", null, new byte[] {1});

        assertEquals(HttpStatus.BAD_REQUEST, statusOf(file));
    }

    @Test
    void nonMediaContentTypeIsRejected() {
        MockMultipartFile pdf = new MockMultipartFile("file", "a.pdf", "application/pdf", new byte[] {1});

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, statusOf(pdf));
    }

    private HttpStatus statusOf(MockMultipartFile file) {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class, () -> resolver.resolve(file));
        return HttpStatus.valueOf(ex.getStatusCode().value());
    }

    /** PNG signature + IHDR + an IDAT chunk header with no data: enough for a header read, impossible to decode. */
    private static byte[] pngHeaderOnly(int width, int height) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(new byte[] {(byte) 0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A});

        ByteArrayOutputStream ihdr = new ByteArrayOutputStream();
        DataOutputStream ihdrData = new DataOutputStream(ihdr);
        ihdrData.writeInt(width);
        ihdrData.writeInt(height);
        ihdrData.write(new byte[] {8, 2, 0, 0, 0}); // 8-bit RGB, no interlace
        writeChunk(out, "IHDR", ihdr.toByteArray());
        writeChunk(out, "IDAT", new byte[0]);
        return out.toByteArray();
    }

    private static void writeChunk(ByteArrayOutputStream out, String type, byte[] data) throws IOException {
        DataOutputStream stream = new DataOutputStream(out);
        stream.writeInt(data.length);
        byte[] typeBytes = type.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
        stream.write(typeBytes);
        stream.write(data);
        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        stream.writeInt((int) crc.getValue());
    }
}
