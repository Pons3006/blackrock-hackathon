package com.ponshankar.hackathon.blackrock.model.request;

import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ValidatorRequest(
        @JsonProperty("wage") Long wage,
        @JsonProperty("transactions") List<Transaction> transactions
) {}
