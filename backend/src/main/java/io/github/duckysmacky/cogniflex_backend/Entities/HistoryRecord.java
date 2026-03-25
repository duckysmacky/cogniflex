package io.github.duckysmacky.cogniflex_backend.Entities;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "history_records")
public class HistoryRecord {

    @Id
    private UUID id;

    @Column(nullable = false, length = 16)
    private String type;

    @Column(nullable = false)
    private int kind;

    @Column(nullable = false)
    private double accuracy;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    protected HistoryRecord() {
    }

    public HistoryRecord(UUID id, String type, int kind, double accuracy, Instant createdAt) {
        this.id = id;
        this.type = type;
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

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getKind() {
        return kind;
    }

    public void setKind(int kind) {
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
