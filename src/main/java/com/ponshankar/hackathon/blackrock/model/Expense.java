package com.ponshankar.hackathon.blackrock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Expense(
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("amount") Long amount
) {}
