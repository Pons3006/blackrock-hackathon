package com.blackrock.hackathon.model.response;

import com.blackrock.hackathon.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FilterResponse(
        @JsonProperty("valid") List<Transaction> valid,
        @JsonProperty("invalid") List<Transaction> invalid
) {}
