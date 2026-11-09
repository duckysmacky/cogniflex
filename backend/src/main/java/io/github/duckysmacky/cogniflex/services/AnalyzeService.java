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
import io.github.duckysmacky.cogniflex.processing.media.MediaInfo;
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
    private final InputLimitValidator inputLimitValidator;
    private final MediaContentValidator mediaContentValidator;

    public AnalyzeService(
        AnalysisOrchestrator analysisOrchestrator,
        ContentItemFactory contentItemFactory,
        TextPreprocessor textPreprocessor,
        MediaParser mediaParser,
        MediaTypeResolver mediaTypeResolver,
        InputLimitValidator inputLimitValidator,
        MediaContentValidator mediaContentValidator
    ) {
        this.analysisOrchestrator = analysisOrchestrator;
        this.contentItemFactory = contentItemFactory;
        this.textPreprocessor = textPreprocessor;
        this.mediaParser = mediaParser;
        this.mediaTypeResolver = mediaTypeResolver;
        this.inputLimitValidator = inputLimitValidator;
        this.mediaContentValidator = mediaContentValidator;
    }

    public AnalysisResultResponse analyzeText(CreateTextDetectionRequest request) {
        long startedAt = System.nanoTime();

        inputLimitValidator.validateTextLength(request.text());
        PreprocessedText text = textPreprocessor.preprocess(request.text(), TextPreprocessingOptions.forModelInput());
        inputLimitValidator.validateWordCount(text);
        ContentItem item = contentItemFactory.fromText(text);

        AnalysisResultSummary result = analysisOrchestrator.submit(item);
        AnalysisResultResponse response = toResponse(result);

        logElapsed("text", startedAt);
        return response;
    }

    public AnalysisResultResponse analyzeMedia(MultipartFile file) {
        long startedAt = System.nanoTime();

        MediaInfo info = mediaTypeResolver.resolve(file);
        mediaContentValidator.validate(info);
        inputLimitValidator.validateMediaSize(info.declaredType(), file);
        if (info.declaredType() == MediaType.VIDEO) {
            throw new UnsupportedAnalysisException("Video analysis is not supported yet");
        }

        ParsedMedia media = mediaParser.parse(file, info);
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
