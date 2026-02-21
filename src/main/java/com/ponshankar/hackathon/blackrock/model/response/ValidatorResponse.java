package com.ponshankar.hackathon.blackrock.model.response;

import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ValidatorResponse(
        @JsonProperty("valid") List<Transaction> valid,
        @JsonProperty("invalid") List<Transaction> invalid,
        @JsonProperty("duplicate") List<Transaction> duplicate
) {}
