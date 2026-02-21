package com.ponshankar.hackathon.blackrock.model.request;

import com.ponshankar.hackathon.blackrock.model.Expense;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ParseRequest(
        @JsonProperty("expenses") List<Expense> expenses
) {}
