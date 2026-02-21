package com.ponshankar.hackathon.blackrock.model.response;

import com.ponshankar.hackathon.blackrock.model.Totals;
import com.ponshankar.hackathon.blackrock.model.Transaction;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ParseResponse(
        @JsonProperty("transactions") List<Transaction> transactions,
        @JsonProperty("totals") Totals totals
) {}
