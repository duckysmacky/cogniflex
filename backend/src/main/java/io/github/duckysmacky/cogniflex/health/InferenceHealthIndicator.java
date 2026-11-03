package io.github.duckysmacky.cogniflex.health;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Reports the connectivity of the gRPC channel to the inference service.
 * The inference service has no health RPC, so channel state is the cheapest reliable signal.
 */
@Component
public class InferenceHealthIndicator implements HealthIndicator {
    private static final Logger log = LoggerFactory.getLogger(InferenceHealthIndicator.class);
    static final Duration CONNECTION_CHECK_TIMEOUT = Duration.ofMillis(500);

    private final ManagedChannel inferenceManagedChannel;

    public InferenceHealthIndicator(ManagedChannel inferenceManagedChannel) {
        this.inferenceManagedChannel = inferenceManagedChannel;
    }

    @Override
    public Health health() {
        var state = currentState();
        var builder = switch (state) {
            case READY -> Health.up();
            case IDLE, CONNECTING -> Health.outOfService();
            case TRANSIENT_FAILURE, SHUTDOWN -> Health.down();
        };
        return builder.withDetail("state", state.name()).build();
    }

    private ConnectivityState currentState() {
        try {
            // requestConnection=true wakes an IDLE channel so the first probe does not report it as down
            var state = inferenceManagedChannel.getState(true);
            return switch (state) {
                case READY, TRANSIENT_FAILURE, SHUTDOWN -> state;
                case IDLE, CONNECTING -> waitForStateChange(state);
            };
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ConnectivityState.TRANSIENT_FAILURE;
        } catch (RuntimeException e) {
            log.warn("Inference channel state check failed", e);
            return ConnectivityState.TRANSIENT_FAILURE;
        }
    }

    private ConnectivityState waitForStateChange(ConnectivityState state) throws InterruptedException {
        var stateChanged = new CountDownLatch(1);
        inferenceManagedChannel.notifyWhenStateChanged(state, stateChanged::countDown);
        stateChanged.await(CONNECTION_CHECK_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);
        return inferenceManagedChannel.getState(false);
    }
}
