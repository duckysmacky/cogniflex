package io.github.duckysmacky.cogniflex.analysis.static_.image;

import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.config.StaticAnalysisConfig;

public abstract class ImageAnalysisRule extends AnalysisRule<ImageAnalysisContext> {
    protected ImageAnalysisRule(String code, Category category, StaticAnalysisConfig config) {
        super(code, category, config);
    }
}
