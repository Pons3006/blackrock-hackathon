package com.blackrock.hackathon.controller;

import com.blackrock.hackathon.model.response.PerformanceResponse;
import com.blackrock.hackathon.service.PerformanceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class PerformanceController {

    private final PerformanceService performanceService;

    public PerformanceController(PerformanceService performanceService) {
        this.performanceService = performanceService;
    }

    @GetMapping("/performance")
    public ResponseEntity<PerformanceResponse> performance() {
        return ResponseEntity.ok(performanceService.getPerformance());
    }
}
