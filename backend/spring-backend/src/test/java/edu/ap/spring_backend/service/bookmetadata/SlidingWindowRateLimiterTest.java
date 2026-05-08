package edu.ap.spring_backend.service.bookmetadata;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class SlidingWindowRateLimiterTest {

    @Test
    void acquire_underThreshold_returns() {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(2, 1000);
        assertDoesNotThrow(() -> {
            limiter.acquire();
            limiter.acquire();
        });
    }
}