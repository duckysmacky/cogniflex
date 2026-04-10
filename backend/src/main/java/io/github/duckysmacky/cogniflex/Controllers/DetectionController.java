package io.github.duckysmacky.cogniflex.Controllers;

import io.github.duckysmacky.cogniflex.Dtos.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.Dtos.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.Enums.MediaType;
import io.github.duckysmacky.cogniflex.Services.DetectionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
        AnalyzeResultResponse response = detectionService.analyzeText(request.text());
        return ResponseEntity.ok(response);
    }

    @PostMapping(value = "/media", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AnalyzeResultResponse> analyzeMedia(
            @RequestPart("file") MultipartFile file
    ) {
        MediaType mediaType = resolveMediaType(file);
        AnalyzeResultResponse response = detectionService.analyzeMedia(mediaType);
        return ResponseEntity.ok(response);
    }

    private MediaType resolveMediaType(MultipartFile file) {
        if (file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "File is empty");
        }

        String contentType = file.getContentType();

        if (contentType == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content type is missing");
        }

        if (contentType.startsWith("image/")) {
            return MediaType.IMAGE;
        }

        if (contentType.startsWith("video/")) {
            return MediaType.VIDEO;
        }

        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported file type");
    }
}
