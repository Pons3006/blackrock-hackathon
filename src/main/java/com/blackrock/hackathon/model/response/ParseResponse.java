package com.blackrock.hackathon.model.response;

import com.blackrock.hackathon.model.Totals;
import com.blackrock.hackathon.model.Transaction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParseResponse(
        @JsonProperty("transactions") List<Transaction> transactions,
        @JsonProperty("totals") Totals totals
) {}
