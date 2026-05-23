package io.github.duckysmacky.cogniflex.entities;

import io.github.duckysmacky.cogniflex.converters.AnalysisVerdictConverter;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.analysis.InputType;
import io.github.duckysmacky.cogniflex.analysis.MediaType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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

    @Convert(converter = AnalysisVerdictConverter.class)
    @ColumnTransformer(write = "?::analysis_verdict")
    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "verdict", nullable = false, columnDefinition = "analysis_verdict")
    private AnalysisVerdict verdict;

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
            AnalysisVerdict verdict,
            double accuracy,
            Instant createdAt
    ) {
        this.id = id;
        this.inputType = inputType;
        this.mediaType = mediaType;
        this.verdict = verdict;
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

    public AnalysisVerdict getVerdict() {
        return verdict;
    }

    public void setVerdict(AnalysisVerdict verdict) {
        this.verdict = verdict;
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
