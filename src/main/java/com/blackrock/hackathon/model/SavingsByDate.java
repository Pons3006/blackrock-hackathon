package com.blackrock.hackathon.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SavingsByDate(
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("amount") Double amount,
        @JsonProperty("profits") Double profits,
        @JsonProperty("taxBenefit") Double taxBenefit
) {}
