package com.ponshankar.hackathon.blackrock.controller;

import com.ponshankar.hackathon.blackrock.model.request.ReturnsRequest;
import com.ponshankar.hackathon.blackrock.model.response.ReturnsResponse;
import com.ponshankar.hackathon.blackrock.service.ReturnsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class ReturnsController {

    private static final Logger log = LoggerFactory.getLogger(ReturnsController.class);

    private final ReturnsService returnsService;

    public ReturnsController(ReturnsService returnsService) {
        this.returnsService = returnsService;
    }

    @PostMapping("/returns:nps")
    public ResponseEntity<ReturnsResponse> nps(@RequestBody ReturnsRequest request) {
        int txnCount = request.transactions() != null ? request.transactions().size() : 0;
        log.info("POST /returns:nps - {} transactions, age={}", txnCount, request.age());
        ReturnsResponse response = returnsService.computeNps(request);
        log.debug("NPS computation completed: totalAmount={}, totalCeiling={}, savingsPeriods={}",
                response.totalAmount(), response.totalCeiling(), response.savingsByDate().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/returns:index")
    public ResponseEntity<ReturnsResponse> index(@RequestBody ReturnsRequest request) {
        int txnCount = request.transactions() != null ? request.transactions().size() : 0;
        log.info("POST /returns:index - {} transactions, age={}", txnCount, request.age());
        ReturnsResponse response = returnsService.computeIndex(request);
        log.debug("Index computation completed: totalAmount={}, totalCeiling={}, savingsPeriods={}",
                response.totalAmount(), response.totalCeiling(), response.savingsByDate().size());
        return ResponseEntity.ok(response);
    }
}
