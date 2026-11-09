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
import io.github.duckysmacky.cogniflex.exceptions.InvalidMediaException;
import io.github.duckysmacky.cogniflex.exceptions.UnsupportedAnalysisException;
import io.github.duckysmacky.cogniflex.processing.media.Dimensions;
import io.github.duckysmacky.cogniflex.processing.media.MediaFormat;
import io.github.duckysmacky.cogniflex.processing.media.MediaInfo;
import io.github.duckysmacky.cogniflex.processing.media.MediaParser;
import io.github.duckysmacky.cogniflex.processing.media.MediaTypeResolver;
import io.github.duckysmacky.cogniflex.processing.media.ParsedMedia;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AnalyzeServiceTest {
    private final AnalysisOrchestrator orchestrator = mock(AnalysisOrchestrator.class);
    private final MediaTypeResolver mediaTypeResolver = mock(MediaTypeResolver.class);
    private final MediaParser mediaParser = mock(MediaParser.class);
    private final TextPreprocessor textPreprocessor = mock(TextPreprocessor.class);
    private final MediaContentValidator mediaContentValidator = mock(MediaContentValidator.class);
    private final InputLimitValidator inputLimitValidator = new InputLimitValidator(
        new InputLimitsProperties(
            new InputLimitsProperties.Text(3, 100),
            new InputLimitsProperties.Media(DataSize.ofBytes(10), DataSize.ofBytes(100), 50_000_000L)
        ),
        new WordTokenizer()
    );
    private final AnalyzeService service = new AnalyzeService(
        orchestrator,
        new ContentItemFactory(),
        textPreprocessor,
        mediaParser,
        mediaTypeResolver,
        inputLimitValidator,
        mediaContentValidator
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
        when(mediaTypeResolver.resolve(image)).thenReturn(imageInfo());

        InputLimitException ex = assertThrows(InputLimitException.class, () -> service.analyzeMedia(image));

        assertEquals("FILE_TOO_LARGE", ex.code());
        verifyNoInteractions(mediaParser, orchestrator);
    }

    @Test
    void oversizedVideoReportsSizeNotUnsupported() {
        MockMultipartFile video = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[101]);
        when(mediaTypeResolver.resolve(video)).thenReturn(videoInfo());

        assertThrows(InputLimitException.class, () -> service.analyzeMedia(video));
    }

    @Test
    void contentValidationFailureStopsTheMediaPipeline() {
        MockMultipartFile fake = new MockMultipartFile("file", "photo.png", "image/png", new byte[] {1, 2, 3});
        MediaInfo info = new MediaInfo(MediaType.IMAGE, null, null);
        when(mediaTypeResolver.resolve(fake)).thenReturn(info);
        doThrow(InvalidMediaException.unsupportedFormat()).when(mediaContentValidator).validate(info);

        InvalidMediaException ex = assertThrows(InvalidMediaException.class, () -> service.analyzeMedia(fake));

        assertEquals("UNSUPPORTED_MEDIA_TYPE", ex.code());
        verifyNoInteractions(mediaParser, orchestrator);
    }

    @Test
    void videoIsValidatedThenRejectedAsUnsupportedWithoutAnalysis() {
        MockMultipartFile video = new MockMultipartFile("file", "clip.mp4", "video/mp4", new byte[] {1, 2, 3});
        MediaInfo info = videoInfo();
        when(mediaTypeResolver.resolve(video)).thenReturn(info);

        UnsupportedAnalysisException ex = assertThrows(
            UnsupportedAnalysisException.class,
            () -> service.analyzeMedia(video)
        );

        assertEquals("Video analysis is not supported yet", ex.getMessage());
        verify(mediaContentValidator).validate(info);
        verifyNoInteractions(orchestrator, mediaParser);
    }

    @Test
    void imageStillGoesThroughAnalysis() {
        byte[] bytes = {1, 2, 3};
        MockMultipartFile image = new MockMultipartFile("file", "photo.png", "image/png", bytes);
        MediaInfo info = imageInfo();
        when(mediaTypeResolver.resolve(image)).thenReturn(info);
        when(mediaParser.parse(image, info)).thenReturn(new ParsedMedia(
            MediaType.IMAGE, bytes, "photo.png", "image/png", bytes.length, "png",
            MediaFormat.PNG, new Dimensions(1, 1)
        ));
        when(orchestrator.submit(any(ContentItem.class))).thenReturn(new AnalysisResultSummary(
            FinalScore.fromAiProbability(ContentType.IMAGE, 0.8, 0.0, 1.0),
            List.of()
        ));

        AnalysisResultResponse response = service.analyzeMedia(image);

        assertEquals(AnalysisVerdict.AI, response.verdict());
        assertEquals(0.8, response.confidence(), 0.0001);
        verify(mediaContentValidator).validate(info);
    }

    private MediaInfo imageInfo() {
        return new MediaInfo(MediaType.IMAGE, MediaFormat.PNG, new Dimensions(1, 1));
    }

    private MediaInfo videoInfo() {
        return new MediaInfo(MediaType.VIDEO, MediaFormat.MP4, null);
    }
}
