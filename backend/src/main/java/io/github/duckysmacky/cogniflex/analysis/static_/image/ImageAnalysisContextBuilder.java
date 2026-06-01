package io.github.duckysmacky.cogniflex.analysis.static_.image;

import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.static_.AnalysisContextBuilder;
import org.springframework.stereotype.Component;

@Component
public class ImageAnalysisContextBuilder implements AnalysisContextBuilder<ImageAnalysisContext> {
    @Override
    public ImageAnalysisContext build(ContentItem item) {
        if (item.contentType() != ContentType.IMAGE) {
            throw new IllegalArgumentException("Expected IMAGE content item");
        }

        // TODO
        return new ImageAnalysisContext(item);
    }
}
