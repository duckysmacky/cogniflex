package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.AnalysisOrchestrator;
import io.github.duckysmacky.cogniflex.analysis.AnalysisResultSummary;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.exceptions.UnsupportedAnalysisException;
import io.github.duckysmacky.cogniflex.processing.media.MediaParser;
import io.github.duckysmacky.cogniflex.processing.media.MediaTypeResolver;
import io.github.duckysmacky.cogniflex.processing.text.TextPreprocessor;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AnalyzeServiceTest {
    private final AnalysisOrchestrator orchestrator = mock(AnalysisOrchestrator.class);
    private final MediaTypeResolver mediaTypeResolver = new MediaTypeResolver();
    private final MediaParser mediaParser = mock(MediaParser.class);
    private final AnalyzeService service = new AnalyzeService(
        orchestrator,
        new ContentItemFactory(),
        mock(TextPreprocessor.class),
        mediaParser,
        mediaTypeResolver
    );

    @Test
    void videoIsRejectedAsUnsupportedWithoutAnalysis() {
        MockMultipartFile video = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[] {1, 2, 3});

        UnsupportedAnalysisException ex = assertThrows(
            UnsupportedAnalysisException.class,
            () -> service.analyzeMedia(video)
        );

        assertEquals("Video analysis is not supported yet", ex.getMessage());
        verifyNoInteractions(orchestrator, mediaParser);
    }

    @Test
    void imageStillGoesThroughAnalysis() {
        byte[] bytes = {1, 2, 3};
        MockMultipartFile image = new MockMultipartFile("file", "photo.png", "image/png", bytes);
        when(mediaParser.parse(image)).thenReturn(new io.github.duckysmacky.cogniflex.processing.media.ParsedMedia(
            MediaType.IMAGE, bytes, "photo.png", "image/png", bytes.length, "png"
        ));
        when(orchestrator.submit(any(ContentItem.class))).thenReturn(new AnalysisResultSummary(
            FinalScore.fromAiProbability(ContentType.IMAGE, 0.8, 0.0, 1.0),
            List.of()
        ));

        AnalysisResultResponse response = service.analyzeMedia(image);

        assertEquals(AnalysisVerdict.AI, response.verdict());
        assertEquals(0.8, response.confidence(), 0.0001);
    }
}
