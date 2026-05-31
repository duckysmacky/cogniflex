package io.github.duckysmacky.cogniflex.analysis.static_.image;

import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticScoreCalculator;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ImageStaticAnalyzer extends StaticAnalyzer<ImageAnalysisContext> {
    private final List<AnalysisRule<ImageAnalysisContext>> rules;

    public ImageStaticAnalyzer(
        ImageAnalysisContextBuilder contextBuilder,
        StaticScoreCalculator scoreCalculator,
        List<ImageAnalysisRule> rules
    ) {
        super(contextBuilder, scoreCalculator);
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
