package com.ponshankar.hackathon.blackrock.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Transaction(
        @JsonProperty("date") String date,
        @JsonProperty("amount") Double amount,
        @JsonProperty("ceiling") Double ceiling,
        @JsonProperty("remanent") Double remanent,
        @JsonProperty("message") String message
) {
    public Transaction(String date, Double amount, Double ceiling, Double remanent) {
        this(date, amount, ceiling, remanent, null);
    }
}
