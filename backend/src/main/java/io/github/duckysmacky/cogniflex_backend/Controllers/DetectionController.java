package io.github.duckysmacky.cogniflex_backend.Controllers;

import io.github.duckysmacky.cogniflex_backend.Dtos.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex_backend.Dtos.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex_backend.Services.DetectionService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analyze")
public class DetectionController {

    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService) {
        this.detectionService = detectionService;
    }

    @PostMapping("/text")
    public ResponseEntity<AnalyzeResultResponse> analyzeText(
            @Valid @RequestBody CreateTextDetectionRequest request
    ) {
        AnalyzeResultResponse response = detectionService.analyzeText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeResultResponse> analyzeMedia(
            @RequestPart("file") MultipartFile file
    ) {
        AnalyzeResultResponse response = detectionService.analyzeMedia(file);
        return ResponseEntity.ok(response);
    }
}
