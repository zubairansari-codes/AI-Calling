package com.gasagency.dsc.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Simple in-memory rate limiter for auth endpoints.
 * Limits requests per IP address within a sliding time window.
 */
@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    @Value("${app.rate-limit.auth.requests-per-minute:10}")
    private int maxRequestsPerMinute;

    @Value("${app.rate-limit.auth.block-duration-minutes:5}")
    private int blockDurationMinutes;

    private final Map<String, RateLimitEntry> requestCounts = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();

        // Only rate limit auth endpoints
        if (!path.startsWith("/auth/")) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientIp = getClientIp(request);
        RateLimitEntry entry = requestCounts.computeIfAbsent(clientIp, k -> new RateLimitEntry());

        // Check if client is blocked
        if (entry.isBlocked()) {
            log.warn("Rate limited request from IP: {} to {}", clientIp, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many requests. Try again later.\"}");
            return;
        }

        // Increment counter and check limit
        long now = System.currentTimeMillis();
        entry.cleanAndIncrement(now, maxRequestsPerMinute, blockDurationMinutes);

        if (entry.isBlocked()) {
            log.warn("IP {} exceeded rate limit ({} req/min) on {}", clientIp, maxRequestsPerMinute, path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write(
                    "{\"status\":429,\"message\":\"Too many login attempts. Please wait before retrying.\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isEmpty()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private static class RateLimitEntry {
        private final AtomicInteger count = new AtomicInteger(0);
        private volatile long windowStart = System.currentTimeMillis();
        private volatile long blockedUntil = 0;

        boolean isBlocked() {
            return System.currentTimeMillis() < blockedUntil;
        }

        synchronized void cleanAndIncrement(long now, int maxRequests, int blockMinutes) {
            // Reset window if a minute has passed
            if (now - windowStart > 60_000) {
                count.set(0);
                windowStart = now;
            }

            int current = count.incrementAndGet();
            if (current > maxRequests) {
                blockedUntil = now + (blockMinutes * 60_000L);
            }
        }
    }
}
