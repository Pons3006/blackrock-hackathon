package com.ponshankar.hackathon.blackrock.controller;

import com.ponshankar.hackathon.blackrock.model.request.FilterRequest;
import com.ponshankar.hackathon.blackrock.model.request.ParseRequest;
import com.ponshankar.hackathon.blackrock.model.request.ValidatorRequest;
import com.ponshankar.hackathon.blackrock.model.response.FilterResponse;
import com.ponshankar.hackathon.blackrock.model.response.ParseResponse;
import com.ponshankar.hackathon.blackrock.model.response.ValidatorResponse;
import com.ponshankar.hackathon.blackrock.service.TransactionFilterService;
import com.ponshankar.hackathon.blackrock.service.TransactionParseService;
import com.ponshankar.hackathon.blackrock.service.TransactionValidatorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class TransactionController {

    private static final Logger log = LoggerFactory.getLogger(TransactionController.class);

    private final TransactionParseService parseService;
    private final TransactionValidatorService validatorService;
    private final TransactionFilterService filterService;

    public TransactionController(TransactionParseService parseService,
                                 TransactionValidatorService validatorService,
                                 TransactionFilterService filterService) {
        this.parseService = parseService;
        this.validatorService = validatorService;
        this.filterService = filterService;
    }

    @PostMapping("/transactions:parse")
    public ResponseEntity<ParseResponse> parse(@RequestBody ParseRequest request) {
        int expenseCount = request.expenses() != null ? request.expenses().size() : 0;
        log.info("POST /transactions:parse - {} expenses", expenseCount);
        ParseResponse response = parseService.parse(request);
        log.debug("Parse completed: {} transactions produced", response.transactions().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions:validator")
    public ResponseEntity<ValidatorResponse> validate(@RequestBody ValidatorRequest request) {
        int txnCount = request.transactions() != null ? request.transactions().size() : 0;
        log.info("POST /transactions:validator - {} transactions", txnCount);
        ValidatorResponse response = validatorService.validate(request);
        log.debug("Validation completed: valid={}, invalid={}, duplicate={}",
                response.valid().size(), response.invalid().size(), response.duplicate().size());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transactions:filter")
    public ResponseEntity<FilterResponse> filter(@RequestBody FilterRequest request) {
        int txnCount = request.transactions() != null ? request.transactions().size() : 0;
        int kCount = request.k() != null ? request.k().size() : 0;
        log.info("POST /transactions:filter - {} transactions, {} k-periods", txnCount, kCount);
        FilterResponse response = filterService.filter(request);
        log.debug("Filter completed: valid={}, invalid={}",
                response.valid().size(), response.invalid().size());
        return ResponseEntity.ok(response);
    }
}
