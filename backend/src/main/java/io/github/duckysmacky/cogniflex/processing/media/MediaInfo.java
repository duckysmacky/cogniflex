package io.github.duckysmacky.cogniflex.processing.media;

import io.github.duckysmacky.cogniflex.analysis.MediaType;

/**
 * What an upload really is, as found by {@link MediaTypeResolver}.
 *
 * @param declaredType kind implied by the client Content-Type
 * @param format       format found from the leading bytes; {@code null} if unrecognised
 * @param dimensions   header dimensions of a recognised image; {@code null} for video or an unreadable header
 */
public record MediaInfo(
    MediaType declaredType,
    MediaFormat format,
    Dimensions dimensions
) { }
