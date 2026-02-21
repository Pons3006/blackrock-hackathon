package com.ponshankar.hackathon.blackrock.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

/**
 * Lightweight interceptor that tracks rolling request-latency stats.
 * Uses lock-free counters for minimal overhead at high concurrency.
 */
@Component
public class RequestLatencyInterceptor implements HandlerInterceptor {

    private static final Logger log = LoggerFactory.getLogger(RequestLatencyInterceptor.class);

    private static final String START_ATTR = "requestStartNanos";

    private final LongAdder totalRequests = new LongAdder();
    private final LongAdder totalLatencyNanos = new LongAdder();
    private final AtomicLong maxLatencyNanos = new AtomicLong(0);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        request.setAttribute(START_ATTR, System.nanoTime());
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        Object startObj = request.getAttribute(START_ATTR);
        if (startObj instanceof Long startNanos) {
            long elapsed = System.nanoTime() - startNanos;
            totalRequests.increment();
            totalLatencyNanos.add(elapsed);
            maxLatencyNanos.accumulateAndGet(elapsed, Math::max);

            double elapsedMs = elapsed / 1_000_000.0;
            log.debug("{} {} completed in {}ms (status={})",
                    request.getMethod(), request.getRequestURI(),
                    String.format("%.2f", elapsedMs), response.getStatus());
            if (elapsedMs > 1000) {
                log.warn("Slow request: {} {} took {}ms",
                        request.getMethod(), request.getRequestURI(),
                        String.format("%.2f", elapsedMs));
            }
        }
    }

    public long getTotalRequests() {
        return totalRequests.sum();
    }

    public double getAverageLatencyMs() {
        long count = totalRequests.sum();
        return count == 0 ? 0.0 : (totalLatencyNanos.sum() / 1_000_000.0) / count;
    }

    public double getMaxLatencyMs() {
        return maxLatencyNanos.get() / 1_000_000.0;
    }
}
