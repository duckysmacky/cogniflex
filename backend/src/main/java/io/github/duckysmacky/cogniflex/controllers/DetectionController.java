package io.github.duckysmacky.cogniflex.controllers;

import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.dto.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.services.AnalyzeService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/analyze")
public class DetectionController {

    private final AnalyzeService analyzeService;

    public DetectionController(AnalyzeService analyzeService) {
        this.analyzeService = analyzeService;
    }

    @PostMapping("/text")
    public ResponseEntity<AnalyzeResultResponse> analyzeText(
            @Valid @RequestBody CreateTextDetectionRequest request
    ) {
        AnalyzeResultResponse response = analyzeService.analyzeText(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/media", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeResultResponse> analyzeMedia(
            @RequestPart("file") MultipartFile file
    ) {
        AnalyzeResultResponse response = analyzeService.analyzeMedia(file);
        return ResponseEntity.ok(response);
    }
}
