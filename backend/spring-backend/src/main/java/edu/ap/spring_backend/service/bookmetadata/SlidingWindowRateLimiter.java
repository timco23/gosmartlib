package edu.ap.spring_backend.service.bookmetadata;

import java.util.ArrayDeque;
import java.util.Deque;

public class SlidingWindowRateLimiter {
    private final int threshold;
    private final long windowMs;
    private final Deque<Long> log = new ArrayDeque<>();


    public SlidingWindowRateLimiter(int threshold, long windowMs) {
        this.threshold = threshold;
        this.windowMs = windowMs;
    }

    /**
     * Blocks until a request slot is available within the sliding window, then claims it.
     * With a 60 req/min limit, 50 back-to-back calls complete instantly instead of taking 50s.
     */
    public void acquire() throws InterruptedException {
        while (true) {
            long waitMs;
            synchronized (this) {
                long now = System.currentTimeMillis();
                evictExpired(now);
                if (log.size() < threshold) {
                    log.addLast(now);
                    return;
                }
                // Sleep until the oldest request falls outside the window
                waitMs = windowMs - (now - log.peekFirst()) + 1;
            }
            Thread.sleep(Math.max(waitMs, 1));
        }
    }

    private void evictExpired(long now) {
        while (!log.isEmpty() && now - log.peekFirst() > windowMs) {
            log.pollFirst();
        }
    }
}
