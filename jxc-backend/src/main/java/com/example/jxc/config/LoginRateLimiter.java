package com.example.jxc.config;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;

@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_DURATION_MS = 300_000; // 5 minutes
    private static final long CLEANUP_INTERVAL_MS = 600_000; // 10 minutes

    private final ConcurrentHashMap<String, AttemptInfo> usernameAttempts = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, AttemptInfo> ipAttempts = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanupScheduler = Executors.newSingleThreadScheduledExecutor();

    public LoginRateLimiter() {
        // Periodic cleanup of expired entries to prevent memory leaks
        cleanupScheduler.scheduleAtFixedRate(this::cleanup, CLEANUP_INTERVAL_MS, CLEANUP_INTERVAL_MS, TimeUnit.MILLISECONDS);
    }

    private static class AttemptInfo {
        AtomicInteger count = new AtomicInteger(0);
        volatile long lockoutUntil = 0;
        volatile long lastAttemptTime = System.currentTimeMillis();

        boolean isLocked() {
            return System.currentTimeMillis() < lockoutUntil;
        }

        void recordFailure() {
            lastAttemptTime = System.currentTimeMillis();
            int newCount = count.incrementAndGet();
            if (newCount >= MAX_ATTEMPTS) {
                lockoutUntil = System.currentTimeMillis() + LOCKOUT_DURATION_MS;
            }
        }

        void reset() {
            count.set(0);
            lockoutUntil = 0;
        }

        boolean isExpired() {
            return System.currentTimeMillis() - lastAttemptTime > CLEANUP_INTERVAL_MS;
        }
    }

    public boolean isBlocked(String username, String ip) {
        AttemptInfo usernameInfo = usernameAttempts.get(username);
        if (usernameInfo != null && usernameInfo.isLocked()) return true;
        AttemptInfo ipInfo = ipAttempts.get(ip);
        if (ipInfo != null && ipInfo.isLocked()) return true;
        return false;
    }

    public void recordFailure(String username, String ip) {
        usernameAttempts.computeIfAbsent(username, k -> new AttemptInfo()).recordFailure();
        ipAttempts.computeIfAbsent(ip, k -> new AttemptInfo()).recordFailure();
    }

    public void resetAttempts(String username, String ip) {
        AttemptInfo usernameInfo = usernameAttempts.get(username);
        if (usernameInfo != null) {
            usernameInfo.reset();
        }
        AttemptInfo ipInfo = ipAttempts.get(ip);
        if (ipInfo != null) {
            ipInfo.reset();
        }
    }

    private void cleanup() {
        long now = System.currentTimeMillis();
        usernameAttempts.entrySet().removeIf(entry -> entry.getValue().isExpired() && !entry.getValue().isLocked());
        ipAttempts.entrySet().removeIf(entry -> entry.getValue().isExpired() && !entry.getValue().isLocked());
    }
}
