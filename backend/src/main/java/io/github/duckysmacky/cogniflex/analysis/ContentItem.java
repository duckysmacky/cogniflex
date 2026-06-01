package io.github.duckysmacky.cogniflex.analysis;

import java.util.Arrays;
import java.util.Map;

public record ContentItem(
    ContentType contentType,
    String sourceUrl,
    String filename,
    byte[] bytes,
    Map<String, String> attributes
) {
    public ContentItem {
        if (contentType == null) {
            throw new IllegalArgumentException("Content type is required");
        }

        bytes = bytes == null ? null : Arrays.copyOf(bytes, bytes.length);
        attributes = attributes == null ? Map.of() : Map.copyOf(attributes);
    }

    @Override
    public byte[] bytes() {
        return bytes == null ? null : Arrays.copyOf(bytes, bytes.length);
    }
}
