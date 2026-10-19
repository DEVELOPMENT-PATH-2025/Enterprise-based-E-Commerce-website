package com.nexusmart.api.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    @Value("${rate.limit.attempts:5}")
    private int rateLimitAttempts;

    @Value("${rate.limit.duration:60}")
    private int rateLimitDuration;

    private final ConcurrentHashMap<String, Bucket> buckets = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return !path.equals("/api/v1/auth/login");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String clientIp = getClientIp(request);
        String username = request.getParameter("email");
        String key = username != null ? username : clientIp;
        
        Bucket bucket = buckets.computeIfAbsent(key, k -> createNewBucket());
        
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(rateLimitDuration));
            
            Map<String, Object> errorResponse = Map.of(
                "type", "https://tools.ietf.org/html/rfc7807",
                "title", "Too Many Requests",
                "status", 429,
                "detail", "Rate limit exceeded. Please try again in " + rateLimitDuration + " seconds.",
                "retryAfter", rateLimitDuration
            );
            
            response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
        }
    }

    private Bucket createNewBucket() {
        int capacity = Math.max(1, rateLimitAttempts); // Ensure capacity is at least 1
        int duration = Math.max(1, rateLimitDuration); // Ensure duration is at least 1 second
        return Bucket.builder()
            .addLimit(limit -> limit.capacity(capacity).refillGreedy(capacity, Duration.ofSeconds(duration)))
            .build();
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
