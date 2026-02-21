package com.blackrock.hackathon.service;

import com.blackrock.hackathon.model.request.ValidatorRequest;
import com.blackrock.hackathon.model.response.ValidatorResponse;
import org.springframework.stereotype.Service;

@Service
public class TransactionValidatorService {

    public ValidatorResponse validate(ValidatorRequest request) {
        // TODO: implement duplicate detection, bounds checks, ceiling/remanent consistency
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
