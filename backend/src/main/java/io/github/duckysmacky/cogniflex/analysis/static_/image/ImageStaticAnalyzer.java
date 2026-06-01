package io.github.duckysmacky.cogniflex.analysis.static_.image;

import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticScoreCalculator;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Component
public class ImageStaticAnalyzer extends StaticAnalyzer<ImageAnalysisContext> {
    private final List<AnalysisRule<ImageAnalysisContext>> rules;

    public ImageStaticAnalyzer(
        ImageAnalysisContextBuilder contextBuilder,
        StaticScoreCalculator scoreCalculator,
        @Qualifier("staticAnalysisExecutor")
        Executor staticAnalysisExecutor,
        List<ImageAnalysisRule> rules
    ) {
        super(contextBuilder, scoreCalculator, staticAnalysisExecutor);
        this.rules = List.copyOf(rules);
    }

    @Override
    protected List<AnalysisRule<ImageAnalysisContext>> rules() {
        return rules;
    }

    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.IMAGE;
    }
}
