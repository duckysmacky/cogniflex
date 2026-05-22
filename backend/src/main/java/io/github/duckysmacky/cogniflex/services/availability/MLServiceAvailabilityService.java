package io.github.duckysmacky.cogniflex.services.availability;

import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;

import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class MLServiceAvailabilityService implements AvailabilityService {
    private static final Duration CONNECTION_CHECK_TIMEOUT = Duration.ofMillis(500);

    private final ManagedChannel mlManagedChannel;

    public MLServiceAvailabilityService(ManagedChannel mlManagedChannel) {
        this.mlManagedChannel = mlManagedChannel;
    }

    @Override
    public boolean isAvailable() {
        return getCurrentState() == ConnectivityState.READY;
    }

    @Override
    public String getStatus() {
        return switch (getCurrentState()) {
            case READY -> "AVAILABLE";
            case IDLE, CONNECTING -> "CONNECTING";
            case TRANSIENT_FAILURE -> "UNAVAILABLE";
            case SHUTDOWN -> "NOT CONNECTED";
        };
    }

    private ConnectivityState getCurrentState() {
        try {
            ConnectivityState state = mlManagedChannel.getState(true);

            if (state == ConnectivityState.READY
                || state == ConnectivityState.TRANSIENT_FAILURE
                || state == ConnectivityState.SHUTDOWN) {
                return state;
            }

            return waitForStateChange(state);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ConnectivityState.TRANSIENT_FAILURE;
        } catch (RuntimeException e) {
            return ConnectivityState.TRANSIENT_FAILURE;
        }
    }

    private ConnectivityState waitForStateChange(ConnectivityState state) throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(1);
        mlManagedChannel.notifyWhenStateChanged(state, latch::countDown);
        latch.await(CONNECTION_CHECK_TIMEOUT.toMillis(), TimeUnit.MILLISECONDS);

        return mlManagedChannel.getState(false);
    }
}
