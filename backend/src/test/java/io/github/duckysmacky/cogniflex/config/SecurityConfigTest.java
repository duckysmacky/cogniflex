package io.github.duckysmacky.cogniflex.config;

import io.github.duckysmacky.cogniflex.analysis.AnalysisVerdict;
import io.github.duckysmacky.cogniflex.controllers.DetectionController;
import io.github.duckysmacky.cogniflex.dto.AnalysisResultResponse;
import io.github.duckysmacky.cogniflex.services.AnalyzeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Outside the dev profile only the public analyze API is reachable: the OpenAPI / Swagger UI paths and the
 * removed status and metrics endpoints must all be rejected by security.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(DetectionController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
class SecurityConfigTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyzeService analyzeService;

    @Test
    void publicAnalyzeEndpointStaysReachable() throws Exception {
        given(analyzeService.analyzeText(any()))
            .willReturn(new AnalysisResultResponse(AnalysisVerdict.HUMAN, 0.9));

        mockMvc.perform(post("/api/analyze/text")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"text\":\"Some text to analyze\"}"))
            .andExpect(status().isOk());
    }

    @Test
    void metricsAndStatusEndpointsAreNotExposed() throws Exception {
        mockMvc.perform(get("/api/metrics"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/api/status"))
            .andExpect(status().isForbidden());
    }

    @Test
    void apiDocsAreDeniedOutsideDevProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/swagger-ui.html"))
            .andExpect(status().isForbidden());
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isForbidden());
    }

    @Test
    void historyStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/history"))
            .andExpect(status().isForbidden());
    }
}
