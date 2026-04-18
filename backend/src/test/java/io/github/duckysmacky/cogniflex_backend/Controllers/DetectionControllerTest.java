package io.github.duckysmacky.cogniflex_backend.Controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.duckysmacky.cogniflex_backend.Dtos.AnalyzeResultResponse;
import io.github.duckysmacky.cogniflex_backend.Dtos.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex_backend.Enums.DetectionKind;
import io.github.duckysmacky.cogniflex_backend.Services.AnalyzeService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DetectionController.class)
public class DetectionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyzeService service;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    @WithMockUser(username = "user")
    public void checkAnalyzeTextIsOk() throws Exception {
        CreateTextDetectionRequest request = new CreateTextDetectionRequest("Some text to analyze");
        AnalyzeResultResponse response = new AnalyzeResultResponse(DetectionKind.HUMAN, 0.9);

        given(service.analyzeText(request)).willReturn(response);

        mockMvc.perform(post("/api/analyze/text").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value(response.kind().getCode()))
                .andExpect(jsonPath("$.accuracy").value(response.accuracy()));
    }

    @Test
    @WithMockUser(username = "user")
    public void checkAnalyzeImageIsOk() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/image_test.png");
        byte[] bytes = resource.getInputStream().readAllBytes();
        MockMultipartFile img = new MockMultipartFile("file", "image_test.png", "image/png", bytes);

        given(service.analyzeMedia(any())).willReturn(
                new AnalyzeResultResponse(DetectionKind.AI_GENERATED, 0.67)
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/analyze/media").file(img)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value(DetectionKind.AI_GENERATED.getCode()));
    }

    @Test
    @WithMockUser(username = "user")
    public void checkAnalyzeVideoIsOk() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/video_test.mp4");
        byte[] bytes = resource.getInputStream().readAllBytes();
        MockMultipartFile vid = new MockMultipartFile("file", "video_test.mp4", "video/mp4", bytes);

        given(service.analyzeMedia(any())).willReturn(
                new AnalyzeResultResponse(DetectionKind.AI_GENERATED, 0.63)
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/analyze/media").file(vid)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.kind").value(DetectionKind.AI_GENERATED.getCode()));
    }
}
