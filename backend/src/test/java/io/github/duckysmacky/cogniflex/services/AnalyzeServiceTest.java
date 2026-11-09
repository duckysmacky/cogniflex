package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.AnalysisOrchestrator;
import io.github.duckysmacky.cogniflex.analysis.AnalysisResultSummary;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.ContentType;
import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.config.InputLimitsProperties;
import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.dto.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.exceptions.InputLimitException;
import io.github.duckysmacky.cogniflex.exceptions.UnsupportedAnalysisException;
import io.github.duckysmacky.cogniflex.processing.media.MediaParser;
import io.github.duckysmacky.cogniflex.processing.media.MediaTypeResolver;
import io.github.duckysmacky.cogniflex.processing.text.PreprocessedText;
import io.github.duckysmacky.cogniflex.processing.text.TextPreprocessor;
import io.github.duckysmacky.cogniflex.processing.text.WordTokenizer;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.util.unit.DataSize;

import java.util.List;
import java.util.Locale;

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
    private final TextPreprocessor textPreprocessor = mock(TextPreprocessor.class);
    private final InputLimitValidator inputLimitValidator = new InputLimitValidator(
        new InputLimitsProperties(
            new InputLimitsProperties.Text(3, 100),
            new InputLimitsProperties.Media(DataSize.ofBytes(10), DataSize.ofBytes(100))
        ),
        new WordTokenizer()
    );
    private final AnalyzeService service = new AnalyzeService(
        orchestrator,
        new ContentItemFactory(),
        textPreprocessor,
        mediaParser,
        mediaTypeResolver,
        inputLimitValidator
    );

    @Test
    void tooShortTextIsRejectedWithoutAnalysis() {
        when(textPreprocessor.preprocess(any(), any())).thenReturn(
            new PreprocessedText("just two", "just two", List.of(), null, Locale.ENGLISH)
        );

        InputLimitException ex = assertThrows(
            InputLimitException.class,
            () -> service.analyzeText(new CreateTextDetectionRequest("just two"))
        );

        assertEquals("TEXT_TOO_SHORT", ex.code());
        verifyNoInteractions(orchestrator);
    }

    @Test
    void tooLongTextIsRejectedBeforePreprocessing() {
        InputLimitException ex = assertThrows(
            InputLimitException.class,
            () -> service.analyzeText(new CreateTextDetectionRequest("word ".repeat(30)))
        );

        assertEquals("TEXT_TOO_LONG", ex.code());
        verifyNoInteractions(textPreprocessor, orchestrator);
    }

    @Test
    void oversizedImageIsRejectedBeforeParsing() {
        MockMultipartFile image = new MockMultipartFile("file", "photo.png", "image/png", new byte[11]);

        InputLimitException ex = assertThrows(InputLimitException.class, () -> service.analyzeMedia(image));

        assertEquals("FILE_TOO_LARGE", ex.code());
        verifyNoInteractions(mediaParser, orchestrator);
    }

    @Test
    void oversizedVideoReportsSizeNotUnsupported() {
        MockMultipartFile video = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[101]);

        assertThrows(InputLimitException.class, () -> service.analyzeMedia(video));
    }

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
