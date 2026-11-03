package io.github.duckysmacky.cogniflex.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Boots the full application with a real management port. Redis is not running in tests,
 * so the readiness status itself is not asserted - only which components it is made of.
 */
@SpringBootTest(
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
    properties = "management.server.port=0"
)
@ActiveProfiles("test")
class ActuatorHealthEndpointTest {

    private static final HttpClient HTTP_CLIENT = HttpClient.newHttpClient();

    @Value("${local.server.port}")
    private int applicationPort;

    @Value("${local.management.port}")
    private int managementPort;

    @Test
    void livenessIsUpWithoutAuthenticationAndHasNoDependencyComponents() throws Exception {
        var response = get(managementPort, "/actuator/health/liveness");

        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("\"status\":\"UP\""), response.body());
        assertFalse(response.body().contains("\"db\""), response.body());
        assertFalse(response.body().contains("\"redis\""), response.body());
        assertFalse(response.body().contains("\"inference\""), response.body());
    }

    @Test
    void readinessReportsDatabaseRedisAndInferenceComponents() throws Exception {
        var response = get(managementPort, "/actuator/health/readiness");

        // 200 or 503 depending on whether Redis/inference are reachable; both are valid readiness answers
        assertTrue(response.statusCode() == 200 || response.statusCode() == 503, response.body());
        assertTrue(response.body().contains("\"db\""), response.body());
        assertTrue(response.body().contains("\"redis\""), response.body());
        assertTrue(response.body().contains("\"inference\""), response.body());
        assertTrue(response.body().contains("\"readinessState\""), response.body());
    }

    @Test
    void onlyHealthEndpointIsExposed() throws Exception {
        // not exposed, so the main chain's denyAll() rejects them; the point is that they are not served
        assertTrue(get(managementPort, "/actuator/env").statusCode() >= 400);
        assertTrue(get(managementPort, "/actuator/beans").statusCode() >= 400);
    }

    @Test
    void actuatorIsNotServedOnThePublicApplicationPort() throws Exception {
        assertTrue(get(applicationPort, "/actuator/health/liveness").statusCode() >= 400);
    }

    private static HttpResponse<String> get(int port, String path) throws IOException, InterruptedException {
        var request = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path)).GET().build();
        return HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
    }
}
