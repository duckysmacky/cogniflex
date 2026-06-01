package io.github.duckysmacky.cogniflex.controllers;

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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.dto.CreateTextDetectionRequest;
import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.services.AnalyzeService;

@ExtendWith(SpringExtension.class)
@WebMvcTest(DetectionController.class)
@ActiveProfiles("test")
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
        AnalysisResultResponse response = new AnalysisResultResponse(AnalysisVerdict.HUMAN, 0.9);

        given(service.analyzeText(request)).willReturn(response);

        mockMvc.perform(post("/api/analyze/text").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value(response.verdict().toString()))
                .andExpect(jsonPath("$.confidence").value(response.confidence()))
                .andExpect(jsonPath("$.evidence").isArray());
    }

    @Test
    @WithMockUser(username = "user")
    public void checkAnalyzeImageIsOk() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/image_test.png");
        byte[] bytes = resource.getInputStream().readAllBytes();
        MockMultipartFile img = new MockMultipartFile("file", "image_test.png", "image/png", bytes);

        given(service.analyzeMedia(any())).willReturn(
                new AnalysisResultResponse(AnalysisVerdict.AI, 0.67)
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/analyze/media").file(img)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value(AnalysisVerdict.AI.toString()));
    }

    @Test
    @WithMockUser(username = "user")
    public void checkAnalyzeVideoIsOk() throws Exception {
        ClassPathResource resource = new ClassPathResource("static/video_test.mp4");
        byte[] bytes = resource.getInputStream().readAllBytes();
        MockMultipartFile vid = new MockMultipartFile("file", "video_test.mp4", "video/mp4", bytes);

        given(service.analyzeMedia(any())).willReturn(
                new AnalysisResultResponse(AnalysisVerdict.AI, 0.63)
        );

        mockMvc.perform(MockMvcRequestBuilders
                        .multipart("/api/analyze/media").file(vid)
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verdict").value(AnalysisVerdict.AI.toString()));
    }
}
