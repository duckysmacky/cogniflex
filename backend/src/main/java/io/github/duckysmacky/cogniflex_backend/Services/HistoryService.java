package io.github.duckysmacky.cogniflex_backend.Services;

import io.github.duckysmacky.cogniflex_backend.Dtos.CreateHistoryRequest;
import io.github.duckysmacky.cogniflex_backend.Dtos.HistoryItemResponse;
import io.github.duckysmacky.cogniflex_backend.Entities.HistoryRecord;
import io.github.duckysmacky.cogniflex_backend.Repositories.HistoryRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
public class HistoryService {

    private final HistoryRepository historyRepository;

    public HistoryService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public List<HistoryItemResponse> getAllHistoryItems() {
        return historyRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"))
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public HistoryItemResponse createHistoryItem(CreateHistoryRequest request) {
        validateRequest(request);

        HistoryRecord record = new HistoryRecord(
                UUID.randomUUID(),
                request.inputType(),
                request.mediaType(),
                request.kind(),
                request.accuracy(),
                Instant.now()
        );

        HistoryRecord savedRecord = historyRepository.save(record);
        return toResponse(savedRecord);
    }

    public HistoryItemResponse getHistoryItemById(UUID id) {
        HistoryRecord record = historyRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "History item not found"
                ));

        return toResponse(record);
    }

    public void deleteHistoryItem(UUID id) {
        if (!historyRepository.existsById(id)) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "History item not found"
            );
        }

        historyRepository.deleteById(id);
    }

    private void validateRequest(CreateHistoryRequest request) {
        if ("TEXT".equals(request.inputType()) && request.mediaType() != null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "mediaType must be null when inputType is TEXT"
            );
        }

        if ("MEDIA".equals(request.inputType()) && request.mediaType() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "mediaType is required when inputType is MEDIA"
            );
        }
    }

    private HistoryItemResponse toResponse(HistoryRecord record) {
        return new HistoryItemResponse(
                record.getId(),
                record.getInputType(),
                record.getMediaType(),
                record.getKind(),
                record.getAccuracy(),
                record.getCreatedAt()
        );
    }
}
