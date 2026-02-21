package com.ponshankar.hackathon.blackrock.service;

import com.ponshankar.hackathon.blackrock.model.request.ValidatorRequest;
import com.ponshankar.hackathon.blackrock.model.response.ValidatorResponse;
import org.springframework.stereotype.Service;

@Service
public class TransactionValidatorService {

    public ValidatorResponse validate(ValidatorRequest request) {
        // TODO: implement duplicate detection, bounds checks, ceiling/remanent consistency
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
