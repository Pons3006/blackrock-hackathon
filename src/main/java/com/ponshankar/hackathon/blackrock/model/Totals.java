package com.ponshankar.hackathon.blackrock.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record Totals(
        @JsonProperty("amountSum") Long amountSum,
        @JsonProperty("ceilingSum") Long ceilingSum,
        @JsonProperty("remanentSum") Long remanentSum
) {}
