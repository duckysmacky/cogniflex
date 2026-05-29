package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContext;

public record TextAnalysisContext(
    ContentItem source
) implements AnalysisContext {

}