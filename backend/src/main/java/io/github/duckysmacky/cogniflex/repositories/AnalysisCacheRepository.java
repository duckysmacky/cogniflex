package io.github.duckysmacky.cogniflex.repositories;

import io.github.duckysmacky.cogniflex.entities.AnalysisCacheRecord;
import io.github.duckysmacky.cogniflex.enums.InputType;
import io.github.duckysmacky.cogniflex.enums.MediaType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AnalysisCacheRepository extends JpaRepository<AnalysisCacheRecord, UUID> {

    Optional<AnalysisCacheRecord> findFirstByInputTypeAndMediaTypeAndHashAlgorithmAndContentHash(
            InputType inputType,
            MediaType mediaType,
            String hashAlgorithm,
            String contentHash
    );

    Optional<AnalysisCacheRecord> findFirstByInputTypeAndMediaTypeIsNullAndHashAlgorithmAndContentHash(
            InputType inputType,
            String hashAlgorithm,
            String contentHash
    );
}
