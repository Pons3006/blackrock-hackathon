package com.ponshankar.hackathon.blackrock.controller;

import com.ponshankar.hackathon.blackrock.model.request.ReturnsRequest;
import com.ponshankar.hackathon.blackrock.model.response.ReturnsResponse;
import com.ponshankar.hackathon.blackrock.service.ReturnsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/blackrock/challenge/v1")
public class ReturnsController {

    private final ReturnsService returnsService;

    public ReturnsController(ReturnsService returnsService) {
        this.returnsService = returnsService;
    }

    @PostMapping("/returns:nps")
    public ResponseEntity<ReturnsResponse> nps(@RequestBody ReturnsRequest request) {
        return ResponseEntity.ok(returnsService.computeNps(request));
    }

    @PostMapping("/returns:index")
    public ResponseEntity<ReturnsResponse> index(@RequestBody ReturnsRequest request) {
        return ResponseEntity.ok(returnsService.computeIndex(request));
    }
}
