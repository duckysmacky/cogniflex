package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticScoreCalculator;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextStaticAnalyzer extends StaticAnalyzer<TextAnalysisContext> {
    private final List<AnalysisRule<TextAnalysisContext>> rules;

    public TextStaticAnalyzer(
        TextAnalysisContextBuilder contextBuilder,
        StaticScoreCalculator scoreCalculator,
        List<TextAnalysisRule> rules
    ) {
        super(contextBuilder, scoreCalculator);
        this.rules = List.copyOf(rules);
    }

    @Override
    protected List<AnalysisRule<TextAnalysisContext>> rules() {
        return rules;
    }

    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.TEXT;
    }
}
