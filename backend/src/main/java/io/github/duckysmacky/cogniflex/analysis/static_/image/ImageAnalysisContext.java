package io.github.duckysmacky.cogniflex.analysis.static_.image;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;

public record ImageAnalysisContext(
    ContentItem source
) implements AnalysisContext {
}
