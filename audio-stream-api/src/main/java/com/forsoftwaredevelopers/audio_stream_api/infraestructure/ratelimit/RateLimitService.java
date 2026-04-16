package com.forsoftwaredevelopers.audio_stream_api.infraestructure.ratelimit;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Service;

@Service
public class RateLimitService {

    private final Map<String, AtomicInteger> ipCounters = new ConcurrentHashMap<>();
    private final Map<String, AtomicInteger> tokenCounters = new ConcurrentHashMap<>();
    private final RateLimitProperties properties;

    public RateLimitService(RateLimitProperties properties) {
        this.properties = properties;
    }

    public boolean isAllowed(String ip) {
        if (!properties.isEnabled()) {
            return true;
        }

        AtomicInteger counter = ipCounters.computeIfAbsent(ip, k -> new AtomicInteger(0));
        int count = counter.incrementAndGet();

        if (count > properties.getIpRequestsPerMinute()) {
            counter.decrementAndGet();
            return false;
        }

        return true;
    }

    public boolean isAllowed(String ip, String token) {
        if (!properties.isEnabled()) {
            return true;
        }

        if (!isAllowed(ip)) {
            return false;
        }

        if (token != null && !token.isBlank()) {
            AtomicInteger counter = tokenCounters.computeIfAbsent(token, k -> new AtomicInteger(0));
            int count = counter.incrementAndGet();

            if (count > properties.getTokenRequestsPerMinute()) {
                counter.decrementAndGet();
                return false;
            }
        }

        return true;
    }

    public void resetCounters() {
        ipCounters.clear();
        tokenCounters.clear();
    }

    public void resetCountersForIp(String ip) {
        ipCounters.remove(ip);
    }

    public void resetCountersForToken(String token) {
        tokenCounters.remove(token);
    }
}