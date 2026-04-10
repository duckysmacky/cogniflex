package io.github.duckysmacky.cogniflex.Repositories;

import io.github.duckysmacky.cogniflex.Entities.HistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface HistoryRepository extends JpaRepository<HistoryRecord, UUID> {
}
