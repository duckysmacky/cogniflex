package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.clients.MLClient;
import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.dto.CreateHistoryItemRequest;
import io.github.duckysmacky.cogniflex.dto.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.enums.InputType;
import io.github.duckysmacky.cogniflex.enums.MediaType;
import io.github.duckysmacky.cogniflex.dto.ContentHash;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.util.Optional;

@Service
public class AnalyzeService {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeService.class);

    private final MLClient mlClient;
    private final HistoryService historyService;
    private final MediaTypeResolver mediaTypeResolver;
    private final AnalysisCacheService analysisCacheService;
    private final ContentHashService contentHashService;

    public AnalyzeService(
            MLClient mlClient,
            HistoryService historyService,
            MediaTypeResolver mediaTypeResolver,
            AnalysisCacheService analysisCacheService,
            ContentHashService contentHashService
    ) {
        this.mlClient = mlClient;
        this.historyService = historyService;
        this.mediaTypeResolver = mediaTypeResolver;
        this.analysisCacheService = analysisCacheService;
        this.contentHashService = contentHashService;
    }

    public AnalyzeResultResponse analyzeText(CreateTextDetectionRequest request) {
        long startedAt = System.nanoTime();

        String normalizedText = normalizeText(request.text());
        ContentHash contentHash = contentHashService.hashText(normalizedText);

        Optional<AnalyzeResultResponse> cachedResponse = analysisCacheService.findCachedResult(
                InputType.TEXT,
                null,
                contentHash.algorithm(),
                contentHash.value()
        );

        if (cachedResponse.isPresent()) {
            AnalyzeResultResponse response = cachedResponse.get();

            recordHistoryItem(
                    InputType.TEXT,
                    null,
                    response
            );

            logCacheHit("text", contentHash, startedAt);
            return response;
        }

        AnalyzeResultResponse response = mlClient.analyzeText(normalizedText);

        saveAnalysisResult(
                InputType.TEXT,
                null,
                textHasher.algorithm(),
                contentHash,
                response
        );

        logElapsed("text", startedAt);
        return response;
    }

    public AnalyzeResultResponse analyzeMedia(MultipartFile file) {
        long startedAt = System.nanoTime();

        MediaType mediaType = mediaTypeResolver.resolve(file);
        byte[] content = readBytes(file);
        ContentHash contentHash = contentHashService.hashMedia(mediaType, content);

        Optional<AnalyzeResultResponse> cachedResponse = analysisCacheService.findCachedResult(
                InputType.MEDIA,
                mediaType,
                contentHash.algorithm(),
                contentHash.value()
        );

        if (cachedResponse.isPresent()) {
            AnalyzeResultResponse response = cachedResponse.get();

            recordHistoryItem(
                    InputType.MEDIA,
                    mediaType,
                    response
            );

            logCacheHit(mediaType.name().toLowerCase(), contentHash, startedAt);
            return response;
        }

        AnalyzeResultResponse response = switch (mediaType) {
            case IMAGE -> mlClient.analyzeImage(content);
            case VIDEO -> mlClient.analyzeVideo(content);
        };

        saveAnalysisResult(
                InputType.MEDIA,
                mediaType,
                hasher.algorithm(),
                contentHash,
                response
        );

        logElapsed(mediaType.name().toLowerCase(), startedAt);
        return response;
    }

    private void saveAnalysisResult(
            InputType inputType,
            MediaType mediaType,
            String hashAlgorithm,
            String contentHash,
            AnalyzeResultResponse response
    ) {
        analysisCacheService.saveResult(
                inputType,
                mediaType,
                hashAlgorithm,
                contentHash,
                response
        );

        recordHistoryItem(inputType, mediaType, response);
    }

    private void recordHistoryItem(
            InputType inputType,
            MediaType mediaType,
            AnalyzeResultResponse response
    ) {
        historyService.createHistoryItem(new CreateHistoryItemRequest(
                inputType,
                mediaType,
                response.kind(),
                response.accuracy()
        ));
    }

    private String normalizeText(String text) {
        String normalizedText = text.trim().replaceAll("\\s+", " ");

        if (normalizedText.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Text is empty");
        }

        return normalizedText;
    }

    private byte[] readBytes(MultipartFile file) {
        try {
            return file.getBytes();
        } catch (IOException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Cannot read uploaded file",
                    ex
            );
        }
    }

    private void logCacheHit(String analysisType, String contentHash, long startedAt) {
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} analysis cache hit for hash {} in {} ms", analysisType, contentHash, elapsedMs);
    }

    private void logElapsed(String analysisType, long startedAt) {
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} analysis completed in {} ms", analysisType, elapsedMs);
    }
}
