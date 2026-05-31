package io.github.duckysmacky.cogniflex.analysis.static_.video;

import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticScoreCalculator;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.Executor;

@Component
public class VideoStaticAnalyzer extends StaticAnalyzer<VideoAnalysisContext> {
    private final List<AnalysisRule<VideoAnalysisContext>> rules;

    public VideoStaticAnalyzer(
        VideoAnalysisContextBuilder contextBuilder,
        StaticScoreCalculator scoreCalculator,
        @Qualifier("staticAnalysisExecutor")
        Executor staticAnalysisExecutor,
        List<VideoAnalysisRule> rules
    ) {
        super(contextBuilder, scoreCalculator, staticAnalysisExecutor);
        this.rules = List.copyOf(rules);
    }

    @Override
    protected List<AnalysisRule<VideoAnalysisContext>> rules() {
        return rules;
    }

    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.VIDEO;
    }
}
