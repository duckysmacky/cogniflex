package io.github.duckysmacky.cogniflex_backend.Controllers;

import io.github.duckysmacky.cogniflex_backend.Dtos.StatusResponse;
import io.github.duckysmacky.cogniflex_backend.Services.StatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class StatusController {

    private final StatusService statusService;

    public StatusController(StatusService statusService) {
        this.statusService = statusService;
    }

    @GetMapping("/api/status")
    public ResponseEntity<StatusResponse> getStatus() {
        return ResponseEntity.ok(statusService.getStatus());
    }
}
