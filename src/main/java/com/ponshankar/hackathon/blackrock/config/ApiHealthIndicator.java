package com.ponshankar.hackathon.blackrock.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Duration;
import java.time.Instant;

@Component("api")
public class ApiHealthIndicator implements HealthIndicator {

    private static final Logger log = LoggerFactory.getLogger(ApiHealthIndicator.class);

    private static final double HEAP_USAGE_WARN_THRESHOLD = 0.85;
    private static final double MAX_LATENCY_WARN_MS = 5_000;

    private final RequestLatencyInterceptor latencyInterceptor;
    private final Instant startTime = Instant.now();

    public ApiHealthIndicator(RequestLatencyInterceptor latencyInterceptor) {
        this.latencyInterceptor = latencyInterceptor;
    }

    @Override
    public Health health() {
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();
        long heapUsed = memoryBean.getHeapMemoryUsage().getUsed();
        long heapMax = memoryBean.getHeapMemoryUsage().getMax();
        double heapRatio = heapMax > 0 ? (double) heapUsed / heapMax : 0;

        double avgLatencyMs = latencyInterceptor.getAverageLatencyMs();
        double maxLatencyMs = latencyInterceptor.getMaxLatencyMs();
        long totalRequests = latencyInterceptor.getTotalRequests();
        Duration uptime = Duration.between(startTime, Instant.now());

        boolean degraded = heapRatio > HEAP_USAGE_WARN_THRESHOLD || maxLatencyMs > MAX_LATENCY_WARN_MS;
        Health.Builder builder = degraded ? Health.down() : Health.up();

        if (degraded) {
            log.warn("Health check DOWN: heapUsage={}%, maxLatency={}ms",
                    String.format("%.1f", heapRatio * 100),
                    String.format("%.2f", maxLatencyMs));
        }

        return builder
                .withDetail("uptime", formatDuration(uptime))
                .withDetail("heapUsedMB", String.format("%.2f", heapUsed / (1024.0 * 1024.0)))
                .withDetail("heapMaxMB", String.format("%.2f", heapMax / (1024.0 * 1024.0)))
                .withDetail("heapUsagePercent", String.format("%.1f%%", heapRatio * 100))
                .withDetail("totalRequests", totalRequests)
                .withDetail("avgLatencyMs", String.format("%.2f", avgLatencyMs))
                .withDetail("maxLatencyMs", String.format("%.2f", maxLatencyMs))
                .build();
    }

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
