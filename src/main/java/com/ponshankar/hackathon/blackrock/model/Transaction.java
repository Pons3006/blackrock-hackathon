package com.ponshankar.hackathon.blackrock.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Transaction(
        @JsonProperty("date") String date,
        @JsonProperty("amount") Long amount,
        @JsonProperty("ceiling") Long ceiling,
        @JsonProperty("remanent") Long remanent,
        @JsonProperty("message") String message
) {
    public Transaction(String date, Long amount, Long ceiling, Long remanent) {
        this(date, amount, ceiling, remanent, null);
    }
}
