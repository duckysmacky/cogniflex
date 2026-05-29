package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisRule;
import io.github.duckysmacky.cogniflex.analysis.static_.StaticAnalyzer;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class TextStaticAnalyzer extends StaticAnalyzer<TextAnalysisContext> {
    private final List<AnalysisRule<TextAnalysisContext>> rules;

    public TextStaticAnalyzer(List<AnalysisRule<TextAnalysisContext>> rules) {
        this.rules = rules;
    }

    @Override
    protected TextAnalysisContext buildContext(ContentItem item) {
        // TODO
        return new TextAnalysisContext(item);
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
