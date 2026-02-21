package com.blackrock.hackathon.model.request;

import com.blackrock.hackathon.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ValidatorRequest(
        @JsonProperty("wage") Long wage,
        @JsonProperty("transactions") List<Transaction> transactions
) {}
