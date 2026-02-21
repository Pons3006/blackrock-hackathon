package com.ponshankar.hackathon.blackrock.model.request;

import com.ponshankar.hackathon.blackrock.model.Period;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record FilterRequest(
        @JsonProperty("q") List<Period> q,
        @JsonProperty("p") List<Period> p,
        @JsonProperty("k") List<Period> k,
        @JsonProperty("transactions") List<Transaction> transactions
) {}
