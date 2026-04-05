package io.github.duckysmacky.cogniflex_backend.Controllers;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.duckysmacky.cogniflex_backend.Dtos.CreateHistoryItemRequest;
import io.github.duckysmacky.cogniflex_backend.Dtos.HistoryItemResponse;
import io.github.duckysmacky.cogniflex_backend.Enums.DetectionKind;
import io.github.duckysmacky.cogniflex_backend.Enums.InputType;
import io.github.duckysmacky.cogniflex_backend.Services.HistoryService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(HistoryController.class)
public class HistoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private HistoryService service;

    private UUID textItemId;
    private UUID imageItemId;
    private HistoryItemResponse textItemResponse;
    private HistoryItemResponse imageItemResponse;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        textItemId = UUID.randomUUID();
        imageItemId = UUID.randomUUID();

        textItemResponse = new HistoryItemResponse(
                textItemId,
                InputType.TEXT,
                null,
                DetectionKind.HUMAN,
                0.8,
                Instant.now());

        imageItemResponse = new HistoryItemResponse(
                imageItemId,
                InputType.MEDIA,
                io.github.duckysmacky.cogniflex_backend.Enums.MediaType.IMAGE,
                DetectionKind.AI_GENERATED,
                0.75,
                Instant.now());
    }

    @Test
    @WithMockUser(username = "user")
    public void checkGetHistoryStatusIsOk()
            throws Exception {

        given(service.getAllHistoryItems()).willReturn(List.of(textItemResponse, imageItemResponse));
        mockMvc.perform(get("/api/history").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "user")
    public void checkCreateHistoryItemIsOk()
            throws Exception {
        objectMapper = new ObjectMapper();
        CreateHistoryItemRequest req = new CreateHistoryItemRequest(
                InputType.TEXT,
                null,
                DetectionKind.AI_GENERATED,
                0.7);
        HistoryItemResponse req_resp = new HistoryItemResponse(
                UUID.randomUUID(),
                req.inputType(),
                req.mediaType(),
                req.kind(),
                req.accuracy(),
                Instant.now());
        System.out.println(objectMapper.writeValueAsString(req));
        given(service.createHistoryItem(req)).willReturn(req_resp);
        mockMvc.perform(post("/api/history").with(csrf()).contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(req_resp.id().toString()));
    }

    @Test
    @WithMockUser(username = "user")
    public void checkGetHistoryItemByIdIsOk()
    throws Exception
    {
        given(service.getHistoryItemById(textItemResponse.id())).willReturn(textItemResponse);
        mockMvc.perform(get(String.format("/api/history/%s", textItemResponse.id().toString())).contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "user")
    public void checkDeleteHistoryItemIsOk()
    throws Exception
    {
        mockMvc.perform(delete(String.format("/api/history/%s", textItemResponse.id().toString())).with(csrf()))
        .andExpect(status().isNoContent());
    }
}