package io.github.duckysmacky.cogniflex.services;

import io.github.duckysmacky.cogniflex.repositories.HistoryRepository;

import java.util.UUID;

import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;

@Service
public class DatabaseAvailabilityService {

    private final HistoryRepository historyRepository;

    DatabaseAvailabilityService(HistoryRepository historyRepository) {
        this.historyRepository = historyRepository;
    }

    public String getStatus()
    {
        try {
            historyRepository.count();
            return "CONNECTED";
        } catch (DataAccessException e)
        {
            return "CONNECTION REFUSED";
        }
    }
}
