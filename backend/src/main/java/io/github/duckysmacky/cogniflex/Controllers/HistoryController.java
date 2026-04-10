package io.github.duckysmacky.cogniflex.Controllers;

import io.github.duckysmacky.cogniflex.Dtos.CreateHistoryItemRequest;

import io.github.duckysmacky.cogniflex.Dtos.HistoryItemResponse;
import io.github.duckysmacky.cogniflex.Services.HistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import io.github.duckysmacky.cogniflex.Enums.InputType;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ResponseEntity<List<HistoryItemResponse>> getHistory() {
        return ResponseEntity.ok(historyService.getAllHistoryItems());
    }

    @PostMapping
public ResponseEntity<HistoryItemResponse> createHistoryItem(
        @Valid @RequestBody CreateHistoryItemRequest request
) {
    validateCreateHistoryItemRequest(request);

    HistoryItemResponse response = historyService.createHistoryItem(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
}

private void validateCreateHistoryItemRequest(CreateHistoryItemRequest request) {
    if (request.inputType() == InputType.TEXT && request.mediaType() != null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "mediaType must be null when inputType is TEXT"
        );
    }

    if (request.inputType() == InputType.MEDIA && request.mediaType() == null) {
        throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "mediaType is required when inputType is MEDIA"
        );
    }
}


    @GetMapping("/{id}")
    public ResponseEntity<HistoryItemResponse> getHistoryItemById(@PathVariable UUID id) {
        return ResponseEntity.ok(historyService.getHistoryItemById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistoryItem(@PathVariable UUID id) {
        historyService.deleteHistoryItem(id);
        return ResponseEntity.noContent().build();
    }
}
