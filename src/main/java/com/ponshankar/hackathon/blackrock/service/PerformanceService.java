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
        String time = String.format("%dh %dm %ds", uptime.toHours(), uptime.toMinutesPart(), uptime.toSecondsPart());

        int threads = threadBean.getThreadCount();

        return new PerformanceResponse(time, memory, threads);
    }
}
