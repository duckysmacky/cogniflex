package io.github.duckysmacky.cogniflex.analysis.static_.video;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class VideoStaticAnalyzer extends StaticAnalyzer<VideoAnalysisContext> {
    private final List<AnalysisRule<VideoAnalysisContext>> rules;

    public VideoStaticAnalyzer(List<AnalysisRule<VideoAnalysisContext>> rules) {
        this.rules = rules;
    }

    @Override
    protected VideoAnalysisContext buildContext(ContentItem item) {
        return new VideoAnalysisContext(item);
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
