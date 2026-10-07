package io.github.duckysmacky.cogniflex.analysis.dynamic;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.dynamic.inference.InferenceClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.concurrent.Executor;

@Component
public class ImageDynamicAnalyzer extends DynamicAnalyzer {
    private final InferenceClient inferenceClient;

    public ImageDynamicAnalyzer(
        InferenceClient inferenceClient,
        @Qualifier("dynamicAnalysisExecutor") Executor dynamicAnalysisExecutor
    ) {
        super(dynamicAnalysisExecutor);
        this.inferenceClient = inferenceClient;
    }

    @Override
    public boolean supports(ContentType type) {
        return type == ContentType.IMAGE;
    }

    @Override
    protected DynamicAnalysisResult analyzeDynamic(ContentItem item) {
        return inferenceClient.analyzeImage(item.bytes());
    }
}
