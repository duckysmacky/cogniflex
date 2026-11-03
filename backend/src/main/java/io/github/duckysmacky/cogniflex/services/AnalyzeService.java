package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.analysis.AnalysisOrchestrator;
import io.github.duckysmacky.cogniflex.analysis.AnalysisResultSummary;
import io.github.duckysmacky.cogniflex.analysis.ContentItem;
import io.github.duckysmacky.cogniflex.analysis.ContentItemFactory;
import io.github.duckysmacky.cogniflex.analysis.score.FinalScore;
import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.dto.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.dto.EvidenceResponse;
import io.github.duckysmacky.cogniflex.processing.media.MediaParser;
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

    public AnalyzeService(
        AnalysisOrchestrator analysisOrchestrator,
        ContentItemFactory contentItemFactory,
        TextPreprocessor textPreprocessor,
        MediaParser mediaParser
    ) {
        this.analysisOrchestrator = analysisOrchestrator;
        this.contentItemFactory = contentItemFactory;
        this.textPreprocessor = textPreprocessor;
        this.mediaParser = mediaParser;
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
