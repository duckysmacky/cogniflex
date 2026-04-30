package io.github.duckysmacky.cogniflex.services;

import org.springframework.stereotype.Service;

@Service
public class ModelAvailabilityService {

    public String getStatus()
    {
        return "NOT_CONNECTED_YET";
    }

}
