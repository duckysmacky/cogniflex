package io.github.duckysmacky.cogniflex.analysis.static_.video;

import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;

public abstract class VideoAnalysisRule extends AnalysisRule<VideoAnalysisContext> {
    protected VideoAnalysisRule(String code, Category category, StaticAnalysisConfig config) {
        super(code, category, config);
    }
}
