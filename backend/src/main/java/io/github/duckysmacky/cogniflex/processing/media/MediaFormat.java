package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;

import java.nio.charset.StandardCharsets;

/** File format identified from the first bytes of an upload, independent of the client-declared Content-Type. */
public enum MediaFormat {
    PNG(MediaType.IMAGE, "image/png"),
    JPEG(MediaType.IMAGE, "image/jpeg"),
    GIF(MediaType.IMAGE, "image/gif"),
    BMP(MediaType.IMAGE, "image/bmp"),
    WEBP(MediaType.IMAGE, "image/webp"),
    MP4(MediaType.VIDEO, "video/mp4"),
    WEBM(MediaType.VIDEO, "video/webm"),
    AVI(MediaType.VIDEO, "video/x-msvideo");

    /** Enough bytes to recognise every supported signature. */
    public static final int SIGNATURE_LENGTH = 16;

    private static final byte[] PNG_MAGIC = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] JPEG_MAGIC = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
    private static final byte[] EBML_MAGIC = {0x1A, 0x45, (byte) 0xDF, (byte) 0xA3};

    private final MediaType mediaType;
    private final String mimeType;

    MediaFormat(MediaType mediaType, String mimeType) {
        this.mediaType = mediaType;
        this.mimeType = mimeType;
    }

    public MediaType mediaType() {
        return mediaType;
    }

    public String mimeType() {
        return mimeType;
    }

    /** Returns the format matching the leading bytes, or {@code null} when none is recognised. */
    public static MediaFormat detect(byte[] head) {
        if (head == null) {
            return null;
        }

        if (startsWith(head, 0, PNG_MAGIC)) {
            return PNG;
        }

        if (startsWith(head, 0, JPEG_MAGIC)) {
            return JPEG;
        }

        if (startsWithAscii(head, 0, "GIF87a") || startsWithAscii(head, 0, "GIF89a")) {
            return GIF;
        }

        if (startsWithAscii(head, 0, "BM")) {
            return BMP;
        }

        if (startsWithAscii(head, 0, "RIFF") && startsWithAscii(head, 8, "WEBP")) {
            return WEBP;
        }
		
        if (startsWithAscii(head, 0, "RIFF") && startsWithAscii(head, 8, "AVI ")) {
            return AVI;
        }

        if (startsWithAscii(head, 4, "ftyp")) {
            return MP4;
        }

        if (startsWith(head, 0, EBML_MAGIC)) {
            return WEBM;
        }

        return null;
    }

    private static boolean startsWithAscii(byte[] head, int offset, String ascii) {
        return startsWith(head, offset, ascii.getBytes(StandardCharsets.US_ASCII));
    }

    private static boolean startsWith(byte[] head, int offset, byte[] expected) {
        if (head.length < offset + expected.length) {
            return false;
        }

        for (int index = 0; index < expected.length; index++) {
            if (head[offset + index] != expected[index]) {
                return false;
            }
        }

        return true;
    }
}
