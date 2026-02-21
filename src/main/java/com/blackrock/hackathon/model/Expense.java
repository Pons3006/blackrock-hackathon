package com.blackrock.hackathon.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Expense(
        @JsonProperty("timestamp") String timestamp,
        @JsonProperty("amount") Long amount
) {}
