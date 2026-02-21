package com.ponshankar.hackathon.blackrock.controller;

import com.ponshankar.hackathon.blackrock.model.response.PerformanceResponse;
import com.ponshankar.hackathon.blackrock.service.PerformanceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class PerformanceController {

    private static final Logger log = LoggerFactory.getLogger(PerformanceController.class);

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/performance")
    public ResponseEntity<PerformanceResponse> performance() {
        log.info("GET /performance");
        PerformanceResponse response = performanceService.getPerformance();
        log.debug("Performance: uptime={}, heap={}, threads={}",
                response.uptime(), response.heapUsed(), response.threadCount());
        return ResponseEntity.ok(response);
    }
}
