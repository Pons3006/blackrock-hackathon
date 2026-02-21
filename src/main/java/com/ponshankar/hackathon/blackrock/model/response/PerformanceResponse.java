package com.ponshankar.hackathon.blackrock.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PerformanceResponse(
        @JsonProperty("time") String time,
        @JsonProperty("memory") String memory,
        @JsonProperty("threads") Integer threads
) {}
