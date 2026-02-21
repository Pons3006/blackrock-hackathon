package com.blackrock.hackathon.model.request;

import com.blackrock.hackathon.model.Period;
import com.blackrock.hackathon.model.Transaction;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReturnsRequest(
        @JsonProperty("age") Integer age,
        @JsonProperty("wage") Long wage,
        @JsonProperty("inflation") Double inflation,
        @JsonProperty("q") List<Period> q,
        @JsonProperty("p") List<Period> p,
        @JsonProperty("k") List<Period> k,
        @JsonProperty("transactions") List<Transaction> transactions
) {}
