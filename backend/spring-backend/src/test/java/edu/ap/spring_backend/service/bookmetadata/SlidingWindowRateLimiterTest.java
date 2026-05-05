package edu.ap.spring_backend.service.bookmetadata;

import org.junit.jupiter.api.Test;

class SlidingWindowRateLimiterTest {

    @Test
    void acquire_underThreshold_returns() throws InterruptedException {
        SlidingWindowRateLimiter limiter = new SlidingWindowRateLimiter(2, 1000);
        limiter.acquire();
        limiter.acquire();
    }
}