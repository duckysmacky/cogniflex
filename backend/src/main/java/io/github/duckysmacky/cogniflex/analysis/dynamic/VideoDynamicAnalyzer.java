package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.ml.MLClient;
import org.springframework.stereotype.Component;

@Component
public class VideoDynamicAnalyzer implements DynamicAnalyzer {
    private final MLClient mlClient;

    public VideoDynamicAnalyzer(MLClient mlClient) {
        this.mlClient = mlClient;
    }

    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.VIDEO;
    }

    @Override
    public DynamicAnalysisResult analyze(ContentItem item) {
        return mlClient.analyzeVideo(item.bytes());
    }
}
