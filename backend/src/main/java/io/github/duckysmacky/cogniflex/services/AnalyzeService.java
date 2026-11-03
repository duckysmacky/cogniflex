package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.AnalysisOrchestrator;
import io.github.duckysmacky.cogniflex.analysis.AnalysisResultSummary;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.dto.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.dto.EvidenceResponse;
import io.github.duckysmacky.cogniflex.analysis.MediaType;
import io.github.duckysmacky.cogniflex.exceptions.UnsupportedAnalysisException;
import io.github.duckysmacky.cogniflex.processing.media.MediaParser;
import io.github.duckysmacky.cogniflex.processing.media.MediaTypeResolver;
import io.github.duckysmacky.cogniflex.processing.media.ParsedMedia;
import io.github.duckysmacky.cogniflex.processing.text.PreprocessedText;
import io.github.duckysmacky.cogniflex.processing.text.TextPreprocessingOptions;
import io.github.duckysmacky.cogniflex.processing.text.TextPreprocessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class AnalyzeService {
    private static final Logger log = LoggerFactory.getLogger(AnalyzeService.class);

    private final AnalysisOrchestrator analysisOrchestrator;
    private final ContentItemFactory contentItemFactory;
    private final TextPreprocessor textPreprocessor;
    private final MediaParser mediaParser;
    private final MediaTypeResolver mediaTypeResolver;

    public AnalyzeService(
        AnalysisOrchestrator analysisOrchestrator,
        ContentItemFactory contentItemFactory,
        TextPreprocessor textPreprocessor,
        MediaParser mediaParser,
        MediaTypeResolver mediaTypeResolver
    ) {
        this.analysisOrchestrator = analysisOrchestrator;
        this.contentItemFactory = contentItemFactory;
        this.textPreprocessor = textPreprocessor;
        this.mediaParser = mediaParser;
        this.mediaTypeResolver = mediaTypeResolver;
    }

    public AnalysisResultResponse analyzeText(CreateTextDetectionRequest request) {
        long startedAt = System.nanoTime();

        PreprocessedText text = textPreprocessor.preprocess(request.text(), TextPreprocessingOptions.forModelInput());
        ContentItem item = contentItemFactory.fromText(text);

        AnalysisResultSummary result = analysisOrchestrator.submit(item);
        AnalysisResultResponse response = toResponse(result);

        logElapsed("text", startedAt);
        return response;
    }

    public AnalysisResultResponse analyzeMedia(MultipartFile file) {
        long startedAt = System.nanoTime();

        // Reject before the (potentially huge) upload is read: video analysis has no real implementation yet
        if (mediaTypeResolver.resolve(file) == MediaType.VIDEO) {
            throw new UnsupportedAnalysisException("Video analysis is not supported yet");
        }

        ParsedMedia media = mediaParser.parse(file);
        ContentItem item = contentItemFactory.fromMedia(media);

        AnalysisResultSummary result = analysisOrchestrator.submit(item);
        AnalysisResultResponse response = toResponse(result);

        logElapsed(media.mediaType().name().toLowerCase(), startedAt);
        return response;
    }

    private AnalysisResultResponse toResponse(AnalysisResultSummary result) {
        FinalScore score = result.score();
        return new AnalysisResultResponse(
            score.verdict(),
            score.confidence(),
            result.evidence().stream()
                .map(EvidenceResponse::from)
                .toList()
        );
    }

    private void logElapsed(String analysisType, long startedAt) {
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} analysis completed in {} ms", analysisType, elapsedMs);
    }
}
