package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.ml.MLClient;
import org.springframework.stereotype.Component;

@Component
public class ImageDynamicAnalyzer implements DynamicAnalyzer {
    private final MLClient mlClient;

    public ImageDynamicAnalyzer(MLClient mlClient) {
        this.mlClient = mlClient;
    }

    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.IMAGE;
    }

    @Override
    public DynamicAnalysisResult analyze(ContentItem item) {
        return mlClient.analyzeImage(item.bytes());
    }
}
