package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.ml.MLClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class TextDynamicAnalyzer extends DynamicAnalyzer {
    private final MLClient mlClient;

    public TextDynamicAnalyzer(
        MLClient mlClient,
        @Qualifier("dynamicAnalysisExecutor") Executor dynamicAnalysisExecutor
    ) {
        super(dynamicAnalysisExecutor);
        this.mlClient = mlClient;
    }

    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.TEXT;
    }

    @Override
    protected DynamicAnalysisResult analyzeDynamic(ContentItem item) {
        String text = item.attributes().get(ContentItemFactory.TEXT_ATTRIBUTE);

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text content item requires a non-empty text attribute");
        }

        return mlClient.analyzeText(text);
    }
}
