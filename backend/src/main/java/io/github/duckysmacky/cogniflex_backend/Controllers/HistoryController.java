package io.github.duckysmacky.cogniflex_backend.Controllers;

import io.github.duckysmacky.cogniflex_backend.Dtos.CreateHistoryRequest;
import io.github.duckysmacky.cogniflex_backend.Dtos.HistoryItemResponse;
import io.github.duckysmacky.cogniflex_backend.Services.HistoryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
        return ResponseEntity.ok(historyService.getAll());
    }

    @PostMapping
    public ResponseEntity<HistoryItemResponse> createHistory(
            @Valid @RequestBody CreateHistoryRequest request
    ) {
        HistoryItemResponse response = historyService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<HistoryItemResponse> getHistoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(historyService.getById(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteHistory(@PathVariable UUID id) {
        historyService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
