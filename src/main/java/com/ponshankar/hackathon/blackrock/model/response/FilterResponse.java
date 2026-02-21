package com.ponshankar.hackathon.blackrock.model.response;

import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FilterResponse(
        @JsonProperty("valid") List<Transaction> valid,
        @JsonProperty("invalid") List<Transaction> invalid
) {}
