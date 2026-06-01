package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;

public interface AnalysisContextBuilder<C extends AnalysisContext> {
    C build(ContentItem item);
}