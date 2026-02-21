package com.blackrock.hackathon.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record Period(
        @JsonProperty("start") String start,
        @JsonProperty("end") String end,
        @JsonProperty("fixed") Long fixed,
        @JsonProperty("extra") Long extra
) {}
