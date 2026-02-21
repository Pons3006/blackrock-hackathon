package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.request.ReturnsRequest;
import com.ponshankar.hackathon.blackrock.model.response.ReturnsResponse;
import org.springframework.stereotype.Service;

@Service
public class ReturnsService {

    private static final double NPS_RATE = 0.0711;
    private static final double INDEX_RATE = 0.1449;

    public ReturnsResponse computeNps(ReturnsRequest request) {
        // TODO: implement NPS returns with tax benefit
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public ReturnsResponse computeIndex(ReturnsRequest request) {
        // TODO: implement index fund returns
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
