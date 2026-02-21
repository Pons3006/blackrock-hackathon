package com.ponshankar.hackathon.blackrock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Totals(
        @JsonProperty("amountSum") Double amountSum,
        @JsonProperty("ceilingSum") Double ceilingSum,
        @JsonProperty("remanentSum") Double remanentSum
) {}
