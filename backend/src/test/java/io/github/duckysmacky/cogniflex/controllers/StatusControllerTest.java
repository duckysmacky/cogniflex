package io.github.duckysmacky.cogniflex.controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import io.github.duckysmacky.cogniflex.config.SecurityConfig;
import io.github.duckysmacky.cogniflex.dto.StatusResponse;
import io.github.duckysmacky.cogniflex.services.StatusService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StatusController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class StatusControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatusService service;

    @Test
    public void checkOutputStatusIsOk()
    throws Exception {
        given(service.getStatus())
            .willReturn(new StatusResponse("UP", "UP", "AVAILABLE", "AVAILABLE", "AVAILABLE"));

        mockMvc.perform(get("/api/status")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.backendHealth").value("UP"))
            .andExpect(jsonPath("$.backendStatus").value("UP"))
            .andExpect(jsonPath("$.MLServiceStatus").value("AVAILABLE"))
            .andExpect(jsonPath("$.databaseStatus").value("AVAILABLE"))
            .andExpect(jsonPath("$.redisStatus").value("AVAILABLE"));
    }
}
