package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.dto.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex.entities.AnalysisCacheRecord;
import io.github.duckysmacky.cogniflex.enums.InputType;
import io.github.duckysmacky.cogniflex.enums.MediaType;
import io.github.duckysmacky.cogniflex.repositories.AnalysisCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class AnalysisCacheService {

    private static final Logger log = LoggerFactory.getLogger(AnalysisCacheService.class);

    private final AnalysisCacheRepository analysisCacheRepository;

    public AnalysisCacheService(AnalysisCacheRepository analysisCacheRepository) {
        this.analysisCacheRepository = analysisCacheRepository;
    }

    @Transactional(readOnly = true)
    public Optional<AnalyzeResultResponse> findCachedResult(
            InputType inputType,
            MediaType mediaType,
            String hashAlgorithm,
            String contentHash
    ) {
        Optional<AnalysisCacheRecord> record = mediaType == null
                ? analysisCacheRepository.findFirstByInputTypeAndMediaTypeIsNullAndHashAlgorithmAndContentHash(
                inputType,
                hashAlgorithm,
                contentHash
        )
                : analysisCacheRepository.findFirstByInputTypeAndMediaTypeAndHashAlgorithmAndContentHash(
                inputType,
                mediaType,
                hashAlgorithm,
                contentHash
        );

        return record.map(this::toResponse);
    }

    @Transactional
    public void saveResult(
            InputType inputType,
            MediaType mediaType,
            String hashAlgorithm,
            String contentHash,
            AnalyzeResultResponse response
    ) {
        Instant now = Instant.now();

        AnalysisCacheRecord record = new AnalysisCacheRecord(
                UUID.randomUUID(),
                inputType,
                mediaType,
                contentHash,
                hashAlgorithm,
                response.kind(),
                response.accuracy(),
                now,
                now
        );

        try {
            analysisCacheRepository.save(record);
        } catch (DataIntegrityViolationException ex) {
            log.debug("Analysis cache record already exists for hash {}", contentHash, ex);
        }
    }

    private AnalyzeResultResponse toResponse(AnalysisCacheRecord record) {
        return new AnalyzeResultResponse(
                record.getKind(),
                record.getAccuracy()
        );
    }
}
