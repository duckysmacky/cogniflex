package io.github.duckysmacky.cogniflex_backend.Controllers;
import jakarta.validation.Valid;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.github.duckysmacky.cogniflex_backend.Dtos.CreateImageDetectionResponse;
import io.github.duckysmacky.cogniflex_backend.Dtos.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex_backend.Dtos.CreateTextDetectionResponse;
import io.github.duckysmacky.cogniflex_backend.Dtos.DetectionResultResponse;

import io.github.duckysmacky.cogniflex_backend.Services.DetectionService;

@RestController
@RequestMapping("/api/detections")
public class DetectionController {
    private final DetectionService detectionService;

    public DetectionController(DetectionService detectionService){
        this.detectionService = detectionService;
    }

     @PostMapping("/text")
    public ResponseEntity<CreateTextDetectionResponse> detectText(
            @Valid @RequestBody CreateTextDetectionRequest request
    ) {
        CreateTextDetectionResponse response = detectionService.analyzeText(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<CreateImageDetectionResponse> detectImage(
            @RequestPart("file") MultipartFile file,
            @RequestPart(value = "sourceUrl", required = false) String sourceUrl
    ) {
        CreateImageDetectionResponse response = detectionService.analyzeImage(file, sourceUrl);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<DetectionResultResponse> getDetectionById(@PathVariable UUID id) {
        DetectionResultResponse response = detectionService.getDetectionById(id);
        return ResponseEntity.ok(response);
    }
}
