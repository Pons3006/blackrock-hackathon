package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.response.PerformanceResponse;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Duration;
import java.time.Instant;

@Service
public class PerformanceService {

    private final Instant startTime = Instant.now();

    public PerformanceResponse getPerformance() {
        ThreadMXBean threadBean = ManagementFactory.getThreadMXBean();
        MemoryMXBean memoryBean = ManagementFactory.getMemoryMXBean();

        long heapUsedBytes = memoryBean.getHeapMemoryUsage().getUsed();
        String memory = String.format("%.2f MB", heapUsedBytes / (1024.0 * 1024.0));

        Duration uptime = Duration.between(startTime, Instant.now());
        long totalMillis = uptime.toMillis();
        long hours = totalMillis / 3_600_000;
        long minutes = (totalMillis % 3_600_000) / 60_000;
        long seconds = (totalMillis % 60_000) / 1_000;
        long millis = totalMillis % 1_000;
        String time = String.format("%02d:%02d:%02d.%03d", hours, minutes, seconds, millis);

        int threads = threadBean.getThreadCount();

        return new PerformanceResponse(time, memory, threads);
    }
}
