package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.inference.InferenceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class TextDynamicAnalyzer extends DynamicAnalyzer {
    private final InferenceClient inferenceClient;

    public TextDynamicAnalyzer(
        InferenceClient inferenceClient,
        @Qualifier("dynamicAnalysisExecutor") Executor dynamicAnalysisExecutor
    ) {
        super(dynamicAnalysisExecutor);
        this.inferenceClient = inferenceClient;
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

        return inferenceClient.analyzeText(text);
    }
}
