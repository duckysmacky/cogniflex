package io.github.duckysmacky.cogniflex.analysis.static_.video;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;

public record VideoAnalysisContext(
    ContentItem source
) implements AnalysisContext {

}