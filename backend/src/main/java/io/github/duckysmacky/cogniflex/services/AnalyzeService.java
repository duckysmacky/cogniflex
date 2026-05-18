package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.clients.MLClient;
import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.dto.CreateHistoryItemRequest;
import io.github.duckysmacky.cogniflex.dto.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.enums.InputType;
import io.github.duckysmacky.cogniflex.enums.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import io.github.duckysmacky.cogniflex.hashing.Hasher;
import org.springframework.beans.factory.annotation.Qualifier;


import java.io.IOException;

@Service
public class AnalyzeService {

    private static final Logger log = LoggerFactory.getLogger(AnalyzeService.class);

    private final MLClient mlClient;
    private final HistoryService historyService;
    private final MediaTypeResolver mediaTypeResolver;
    private final Hasher<String> textHasher;
    private final Hasher<byte[]> photoHasher;
    private final Hasher<byte[]> videoHasher;


    public AnalyzeService(
            MLClient mlClient,
            HistoryService historyService,
            MediaTypeResolver mediaTypeResolver,
            @Qualifier("textHasher") Hasher<String> textHasher,
            @Qualifier("photoHasher") Hasher<byte[]> photoHasher,
            @Qualifier("videoHasher") Hasher<byte[]> videoHasher
    ) {
        this.mlClient = mlClient;
        this.historyService = historyService;
        this.mediaTypeResolver = mediaTypeResolver;
        this.textHasher = textHasher;
        this.photoHasher = photoHasher;
        this.videoHasher = videoHasher;
    }


    public AnalyzeResultResponse analyzeText(CreateTextDetectionRequest request) {
        long startedAt = System.nanoTime();

        String normalizedText = normalizeText(request.text());
        AnalyzeResultResponse response = mlClient.analyzeText(normalizedText);

        historyService.createHistoryItem(new CreateHistoryItemRequest(
                InputType.TEXT,
                null,
                response.kind(),
                response.accuracy()
        ));

        logElapsed("text", startedAt);
        return response;
    }

    public AnalyzeResultResponse analyzeMedia(MultipartFile file) {
        long startedAt = System.nanoTime();

        MediaType mediaType = mediaTypeResolver.resolve(file);
        byte[] content = readBytes(file);

        AnalyzeResultResponse response = switch (mediaType) {
            case IMAGE -> mlClient.analyzeImage(content);
            case VIDEO -> mlClient.analyzeVideo(content);
        };

        historyService.createHistoryItem(new CreateHistoryItemRequest(
                InputType.MEDIA,
                mediaType,
                response.kind(),
                response.accuracy()
        ));

        logElapsed(mediaType.name().toLowerCase(), startedAt);
        return response;
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

    private void logElapsed(String analysisType, long startedAt) {
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000;
        log.info("{} analysis completed in {} ms", analysisType, elapsedMs);
    }
}
