package com.example.jxc.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 300_000; // 5 minutes

    private final ConcurrentHashMap<String, AttemptInfo> attempts = new ConcurrentHashMap<>();

    private static class AttemptInfo {
        AtomicInteger count = new AtomicInteger(0);
        long lockoutUntil = 0;

        boolean isLocked() {
            return System.currentTimeMillis() < lockoutUntil;
        }

        void recordFailure() {
            int newCount = count.incrementAndGet();
            if (newCount >= MAX_ATTEMPTS) {
                lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            }
        }

        void reset() {
            count.set(0);
            lockoutUntil = 0;
        }
    }

    public boolean isBlocked(String username) {
        AttemptInfo info = attempts.get(username);
        if (info == null) return false;
        return info.isLocked();
    }

    public void recordFailure(String username) {
        attempts.computeIfAbsent(username, k -> new AttemptInfo()).recordFailure();
    }

    public void resetAttempts(String username) {
        AttemptInfo info = attempts.get(username);
        if (info != null) {
            info.reset();
        }
    }
}
