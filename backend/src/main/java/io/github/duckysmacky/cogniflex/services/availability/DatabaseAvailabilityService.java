package io.github.duckysmacky.cogniflex.services.availability;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

@Service
public class DatabaseAvailabilityService implements AvailabilityService {
    private final JdbcTemplate jdbcTemplate;

    DatabaseAvailabilityService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public boolean isAvailable() {
        try {
            jdbcTemplate.queryForObject("select 1", Integer.class);
            return true;
        } catch (DataAccessException e) {
            return false;
        }
    }

    @Override
    public String getStatus() {
        return isAvailable()
            ? "AVAILABLE"
            : "UNAVAILABLE";
    }
}
