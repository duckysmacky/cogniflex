package io.github.duckysmacky.cogniflex.health;

import io.github.duckysmacky.cogniflex.grpc.AnalyzerGrpc;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.Server;
import io.grpc.inprocess.InProcessChannelBuilder;
import io.grpc.inprocess.InProcessServerBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InferenceHealthIndicatorTest {

    private Server server;
    private ManagedChannel channel;

    @AfterEach
    void tearDown() {
        if (channel != null) {
            channel.shutdownNow();
        }
        if (server != null) {
            server.shutdownNow();
        }
    }

    @Test
    void reportsUpWhenInferenceServerIsReachable() throws IOException {
        var serverName = InProcessServerBuilder.generateName();
        server = InProcessServerBuilder.forName(serverName)
            .directExecutor()
            .addService(new AnalyzerGrpc.AnalyzerImplBase() {})
            .build()
            .start();
        channel = InProcessChannelBuilder.forName(serverName).directExecutor().build();

        var health = new InferenceHealthIndicator(channel).health();

        assertEquals(Status.UP, health.getStatus());
        assertEquals(ConnectivityState.READY.name(), health.getDetails().get("state"));
    }

    @Test
    void reportsDownWhenNothingListensOnTheChannelAddress() {
        channel = InProcessChannelBuilder.forName(InProcessServerBuilder.generateName())
            .directExecutor()
            .build();

        var health = new InferenceHealthIndicator(channel).health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(ConnectivityState.TRANSIENT_FAILURE.name(), health.getDetails().get("state"));
    }

    @Test
    void reportsDownWhenChannelWasShutDown() {
        channel = InProcessChannelBuilder.forName(InProcessServerBuilder.generateName())
            .directExecutor()
            .build();
        channel.shutdownNow();

        var health = new InferenceHealthIndicator(channel).health();

        assertEquals(Status.DOWN, health.getStatus());
        assertEquals(ConnectivityState.SHUTDOWN.name(), health.getDetails().get("state"));
    }

    @Test
    void reportsOutOfServiceWhenChannelStaysConnecting() {
        var connectingChannel = mock(ManagedChannel.class);
        when(connectingChannel.getState(true)).thenReturn(ConnectivityState.CONNECTING);
        when(connectingChannel.getState(false)).thenReturn(ConnectivityState.CONNECTING);

        var health = new InferenceHealthIndicator(connectingChannel).health();

        assertEquals(Status.OUT_OF_SERVICE, health.getStatus());
        assertEquals(ConnectivityState.CONNECTING.name(), health.getDetails().get("state"));
    }

    @Test
    void reportsDownWhenStateCheckThrows() {
        var brokenChannel = mock(ManagedChannel.class);
        when(brokenChannel.getState(true)).thenThrow(new IllegalStateException("channel broken"));

        Health health = new InferenceHealthIndicator(brokenChannel).health();

        assertEquals(Status.DOWN, health.getStatus());
    }
}
