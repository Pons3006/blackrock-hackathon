package com.ponshankar.hackathon.blackrock.config;

import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.stereotype.Component;

import java.lang.management.ManagementFactory;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;

@Component
public class ApplicationInfoContributor implements InfoContributor {

    private final Instant startTime = Instant.now();
    private final RequestLatencyInterceptor latencyInterceptor;

    public ApplicationInfoContributor(RequestLatencyInterceptor latencyInterceptor) {
        this.latencyInterceptor = latencyInterceptor;
    }

    @Override
    public void contribute(Info.Builder builder) {
        Duration uptime = Duration.between(startTime, Instant.now());
        int threadCount = ManagementFactory.getThreadMXBean().getThreadCount();

        builder.withDetail("runtime", Map.of(
                "uptime", formatDuration(uptime),
                "activeThreads", threadCount,
                "totalRequests", latencyInterceptor.getTotalRequests(),
                "avgLatencyMs", String.format("%.2f", latencyInterceptor.getAverageLatencyMs()),
                "maxLatencyMs", String.format("%.2f", latencyInterceptor.getMaxLatencyMs())
        ));
    }

    private String formatDuration(Duration d) {
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        long seconds = d.toSecondsPart();
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
