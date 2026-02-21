package com.blackrock.hackathon.controller;

import com.blackrock.hackathon.model.request.FilterRequest;
import com.blackrock.hackathon.model.request.ParseRequest;
import com.blackrock.hackathon.model.request.ValidatorRequest;
import com.blackrock.hackathon.model.response.FilterResponse;
import com.blackrock.hackathon.model.response.ParseResponse;
import com.blackrock.hackathon.model.response.ValidatorResponse;
import com.blackrock.hackathon.service.TransactionFilterService;
import com.blackrock.hackathon.service.TransactionParseService;
import com.blackrock.hackathon.service.TransactionValidatorService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class TransactionController {

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
        return ResponseEntity.ok(parseService.parse(request));
    }

    @PostMapping("/transactions:validator")
    public ResponseEntity<ValidatorResponse> validate(@RequestBody ValidatorRequest request) {
        return ResponseEntity.ok(validatorService.validate(request));
    }

    @PostMapping("/transactions:filter")
    public ResponseEntity<FilterResponse> filter(@RequestBody FilterRequest request) {
        return ResponseEntity.ok(filterService.filter(request));
    }
}
