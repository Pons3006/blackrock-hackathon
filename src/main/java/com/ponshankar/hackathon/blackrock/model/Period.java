package com.ponshankar.hackathon.blackrock.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Period(
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("fixed") Double fixed,
        @JsonProperty("extra") Double extra
) {}
