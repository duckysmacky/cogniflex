package io.github.duckysmacky.cogniflex.config;

import io.github.duckysmacky.cogniflex.controllers.DetectionController;
import io.github.duckysmacky.cogniflex.services.AnalyzeService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * In the dev profile security lets the OpenAPI paths through. This slice has no springdoc handlers,
 * so a request that passes security ends up as 404 instead of the 403 that security gives in other profiles.
 */
@ExtendWith(SpringExtension.class)
@WebMvcTest(DetectionController.class)
@Import(SecurityConfig.class)
// "test" keeps the profile-gated rate limiting beans out of this slice; "dev" is what the security gate checks
@ActiveProfiles({"test", "dev"})
class SecurityConfigDevProfileTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AnalyzeService analyzeService;

    @Test
    void apiDocsPassSecurityInDevProfile() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
            .andExpect(status().isNotFound());
        mockMvc.perform(get("/swagger-ui/index.html"))
            .andExpect(status().isNotFound());
    }

    @Test
    void metricsEndpointStaysDeniedInDevProfile() throws Exception {
        mockMvc.perform(get("/api/metrics"))
            .andExpect(status().isForbidden());
    }
}
