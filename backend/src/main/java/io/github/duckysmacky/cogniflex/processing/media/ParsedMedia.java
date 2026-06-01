package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;

public record ParsedMedia(
    MediaType mediaType,
    byte[] bytes,
    String filename,
    String contentType,
    long size,
    String extension
) { }
