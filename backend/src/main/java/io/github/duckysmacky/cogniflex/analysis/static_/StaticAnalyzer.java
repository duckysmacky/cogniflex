package io.github.duckysmacky.cogniflex.analysis.static_;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;

public interface StaticAnalyzer {
    boolean supports(ContentType type);
    StaticAnalysisResult analyze(ContentItem item);
}
