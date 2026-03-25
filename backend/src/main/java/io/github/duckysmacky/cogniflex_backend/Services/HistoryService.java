package io.github.duckysmacky.cogniflex_backend.Services;

import io.github.duckysmacky.cogniflex_backend.Dtos.CreateHistoryRequest;
import io.github.duckysmacky.cogniflex_backend.Dtos.HistoryItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class HistoryService {

    private final Map<UUID, HistoryItemResponse> storage = new ConcurrentHashMap<>();

    public List<HistoryItemResponse> getAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(HistoryItemResponse::createdAt).reversed())
                .toList();
    }

    public HistoryItemResponse create(CreateHistoryRequest request) {
        HistoryItemResponse response = new HistoryItemResponse(
                UUID.randomUUID(),
                request.inputType(),
                request.mediaType(),
                request.kind(),
                request.accuracy(),
                Instant.now().toString()
        );

        storage.put(response.id(), response);
        return response;
    }

    public HistoryItemResponse getById(UUID id) {
        HistoryItemResponse item = storage.get(id);

        if (item == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "History item not found");
        }

        return item;
    }

    public void delete(UUID id) {
        HistoryItemResponse removed = storage.remove(id);

        if (removed == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "History item not found");
        }
    }
}
