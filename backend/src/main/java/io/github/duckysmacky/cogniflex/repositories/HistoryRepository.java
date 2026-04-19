package io.github.duckysmacky.cogniflex.repositories;

import io.github.duckysmacky.cogniflex.entities.HistoryRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface HistoryRepository extends JpaRepository<HistoryRecord, UUID> {
}
