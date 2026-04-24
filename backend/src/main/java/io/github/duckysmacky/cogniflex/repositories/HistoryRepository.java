package io.github.duckysmacky.cogniflex.repositories;

import io.github.duckysmacky.cogniflex.entities.HistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HistoryRepository extends JpaRepository<HistoryRecord, UUID> {
}
