package io.github.duckysmacky.cogniflex.services;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import io.github.duckysmacky.cogniflex.config.RateLimitProperties;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RateLimitInterceptorTest {
    private static final int CAPACITY = 2;

    private final RateLimitInterceptor interceptor = new RateLimitInterceptor(
        new InMemoryRateLimitService(
            new RateLimitProperties(List.of("/api/analyze/**"), CAPACITY, CAPACITY, 3600, true)
        )
    );

    @Test
    void clientsWithDifferentAddressesHaveIndependentLimits() throws Exception {
        for (int i = 0; i < CAPACITY; i++) {
            assertTrue(call("203.0.113.1").allowed());
        }

        Result exhausted = call("203.0.113.1");
        assertFalse(exhausted.allowed());
        assertEquals(429, exhausted.status());

        assertTrue(call("203.0.113.2").allowed());
    }

    private Result call(String remoteAddress) throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/analyze/text");
        request.setRemoteAddr(remoteAddress);
        MockHttpServletResponse response = new MockHttpServletResponse();
        boolean allowed = interceptor.preHandle(request, response, new Object());
        return new Result(allowed, response.getStatus());
    }

    private record Result(boolean allowed, int status) {
    }
}
