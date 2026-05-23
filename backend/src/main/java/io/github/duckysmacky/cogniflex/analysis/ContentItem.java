package io.github.duckysmacky.cogniflex.analysis;

import java.util.Map;

public record ContentItem(
    ContentType contentType,
    String sourceUrl,
    String filename,
    byte[] bytes,
    Map<String, String> attributes
) { }
