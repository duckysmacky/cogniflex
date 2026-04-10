package io.github.duckysmacky.cogniflex.Entities;

import io.github.duckysmacky.cogniflex.Converters.DetectionKindConverter;
import io.github.duckysmacky.cogniflex.Enums.DetectionKind;
import io.github.duckysmacky.cogniflex.Enums.InputType;
import io.github.duckysmacky.cogniflex.Enums.MediaType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "history_records")
public class HistoryRecord {

    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "input_type", nullable = false, length = 16)
    private InputType inputType;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 16)
    private MediaType mediaType;

    @Convert(converter = DetectionKindConverter.class)
    @Column(nullable = false)
    private DetectionKind kind;

    @Column(nullable = false)
    private double accuracy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HistoryRecord() {
    }

    public HistoryRecord(
            UUID id,
            InputType inputType,
            MediaType mediaType,
            DetectionKind kind,
            double accuracy,
            Instant createdAt
    ) {
        this.id = id;
        this.inputType = inputType;
        this.mediaType = mediaType;
        this.kind = kind;
        this.accuracy = accuracy;
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public InputType getInputType() {
        return inputType;
    }

    public void setInputType(InputType inputType) {
        this.inputType = inputType;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    public DetectionKind getKind() {
        return kind;
    }

    public void setKind(DetectionKind kind) {
        this.kind = kind;
    }

    public double getAccuracy() {
        return accuracy;
    }

    public void setAccuracy(double accuracy) {
        this.accuracy = accuracy;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
