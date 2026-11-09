package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.config.InputLimitsProperties;
import io.github.duckysmacky.cogniflex.exceptions.InputLimitException;
import io.github.duckysmacky.cogniflex.exceptions.InvalidMediaException;
import io.github.duckysmacky.cogniflex.processing.media.MediaInfo;
import org.springframework.stereotype.Component;

/** Decides whether an inspected upload may be analyzed: real format, honest Content-Type, sane image size. */
@Component
public class MediaContentValidator {
    private final InputLimitsProperties limits;

    public MediaContentValidator(InputLimitsProperties limits) {
        this.limits = limits;
    }

    public void validate(MediaInfo info) {
        if (info.format() == null) {
            throw InvalidMediaException.unsupportedFormat();
        }

        if (info.format().mediaType() != info.declaredType()) {
            throw InvalidMediaException.typeMismatch(info.declaredType(), info.format().mediaType());
        }

        if (info.format().mediaType() != MediaType.IMAGE) {
            return;
        }

        if (info.dimensions() == null) {
            throw InvalidMediaException.invalidImage();
        }

        long maxPixels = limits.media().maxImagePixels();

        if (info.dimensions().pixels() > maxPixels) {
            throw InputLimitException.imageTooLarge(info.dimensions().pixels(), maxPixels);
        }
    }
}
