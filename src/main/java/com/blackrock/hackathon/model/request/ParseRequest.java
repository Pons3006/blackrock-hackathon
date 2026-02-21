package com.blackrock.hackathon.model.request;

import com.blackrock.hackathon.model.Expense;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ParseRequest(
        @JsonProperty("expenses") List<Expense> expenses
) {}
