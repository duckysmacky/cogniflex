package io.github.duckysmacky.cogniflex_backend.Services;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import io.github.duckysmacky.cogniflex_backend.Dtos.CreateImageDetectionResponse;
import io.github.duckysmacky.cogniflex_backend.Dtos.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex_backend.Dtos.CreateTextDetectionResponse;
import io.github.duckysmacky.cogniflex_backend.Dtos.DetectionResultResponse;

@Service
public class DetectionService {
    // ЗАГЛУШКИ !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public CreateTextDetectionResponse analyzeText(CreateTextDetectionRequest request) {
        return new CreateTextDetectionResponse(UUID.randomUUID(), "AI_GENERATED", 0.91);
    }

    public CreateImageDetectionResponse analyzeImage(MultipartFile file, String sourceUrl) {
        return new CreateImageDetectionResponse(UUID.randomUUID(), "AI_GENERATED", 0.87);
    }

    public DetectionResultResponse getDetectionById(UUID id) {
        return new DetectionResultResponse(id, "TEXT", "AI_GENERATED", 0.91, "2026-03-15T21:00:00Z");
    }
}