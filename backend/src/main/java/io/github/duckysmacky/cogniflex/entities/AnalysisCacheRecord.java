package io.github.duckysmacky.cogniflex.entities;

import io.github.duckysmacky.cogniflex.converters.DetectionKindConverter;
import io.github.duckysmacky.cogniflex.enums.DetectionKind;
import io.github.duckysmacky.cogniflex.enums.InputType;
import io.github.duckysmacky.cogniflex.enums.MediaType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "analysis_cache_records")
public class AnalysisCacheRecord {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 16)
    private InputType inputType;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 16)
    private MediaType mediaType;

    @Column(name = "content_hash", nullable = false, length = 64)
    private String contentHash;

    @Column(name = "hash_algorithm", nullable = false, length = 32)
    private String hashAlgorithm;

    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(nullable = false, columnDefinition = "detection_kind")
    private DetectionKind kind;

    @Column(nullable = false)
    private double accuracy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected AnalysisCacheRecord() {
    }

    public AnalysisCacheRecord(
            UUID id,
            InputType inputType,
            MediaType mediaType,
            String contentHash,
            String hashAlgorithm,
            DetectionKind kind,
            double accuracy,
            Instant createdAt,
            Instant updatedAt
    ) {
        this.id = id;
        this.inputType = inputType;
        this.mediaType = mediaType;
        this.contentHash = contentHash;
        this.hashAlgorithm = hashAlgorithm;
        this.kind = kind;
        this.accuracy = accuracy;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public InputType getInputType() {
        return inputType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public String getContentHash() {
        return contentHash;
    }

    public String getHashAlgorithm() {
        return hashAlgorithm;
    }

    public DetectionKind getKind() {
        return kind;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
