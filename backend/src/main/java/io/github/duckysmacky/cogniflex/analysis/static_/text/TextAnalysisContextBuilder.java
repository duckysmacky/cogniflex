package io.github.duckysmacky.cogniflex.analysis.static_.text;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContextBuilder;
import org.springframework.stereotype.Component;

@Component
public class TextAnalysisContextBuilder implements AnalysisContextBuilder<TextAnalysisContext> {
    @Override
    public TextAnalysisContext build(ContentItem item) {
        if (item.contentType() != ContentType.TEXT) {
            throw new IllegalArgumentException("Expected TEXT content item");
        }

        // TODO
        return new TextAnalysisContext(item);
    }
}