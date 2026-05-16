package io.github.duckysmacky.cogniflex.analysis;

public record ContentItem(
    String id,
    ContentType contentType,
    String sourceUrl,
    String filename,
    String mimeType,
    byte[] bytes,
    String text
) { }
