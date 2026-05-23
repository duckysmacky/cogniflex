package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.dynamic.ml.MLClient;
import org.springframework.stereotype.Component;

@Component
public class DynamicAnalyzer {
    private final MLClient mlClient;

    public DynamicAnalyzer(MLClient mlClient) {
        this.mlClient = mlClient;
    }

    public DynamicAnalysisResult analyze(ContentItem item) {
        if (item == null) {
            throw new IllegalArgumentException("Content item is required");
        }

        return switch (item.contentType()) {
            case TEXT -> mlClient.analyzeText(textAttribute(item));
            case IMAGE -> mlClient.analyzeImage(item.bytes());
            case VIDEO -> mlClient.analyzeVideo(item.bytes());
        };
    }

    private String textAttribute(ContentItem item) {
        String text = item.attributes().get(ContentItemFactory.TEXT_ATTRIBUTE);

        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Text content item requires a non-empty text attribute");
        }

        return text;
    }
}
