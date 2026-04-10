package io.github.duckysmacky.cogniflex.Services;

import io.github.duckysmacky.cogniflex.Dtos.CreateHistoryItemRequest;
import io.github.duckysmacky.cogniflex.Dtos.HistoryItemResponse;
import io.github.duckysmacky.cogniflex.Entities.HistoryRecord;
import io.github.duckysmacky.cogniflex.Repositories.HistoryRepository;
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

    public HistoryItemResponse createHistoryItem(CreateHistoryItemRequest request) {

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
