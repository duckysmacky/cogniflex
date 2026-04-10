package io.github.duckysmacky.cogniflex.Controllers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.BDDMockito.given;

import java.time.Instant;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import io.github.duckysmacky.cogniflex.Dtos.StatusResponse;
import io.github.duckysmacky.cogniflex.Services.StatusService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(StatusController.class)
public class StatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private StatusService service;

    @Test
    @WithMockUser(username = "user")
    public void checkOutputStatusIsOk()
    throws Exception {
            given(service.getStatus()).willReturn(new StatusResponse("UP", "UP", "NOT_CONNECTED_YET", Instant.now().toString()));
            mockMvc.perform(get("/api/status")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.backend").value("UP"))
            .andExpect(jsonPath("$.model").value("NOT_CONNECTED_YET"));
    }
}
