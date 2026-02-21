package com.ponshankar.hackathon.blackrock.model.response;

import com.ponshankar.hackathon.blackrock.model.SavingsByDate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReturnsResponse(
        @JsonProperty("transactionsTotalAmount") Double transactionsTotalAmount,
        @JsonProperty("transactionsTotalCeiling") Double transactionsTotalCeiling,
        @JsonProperty("savingsByDates") List<SavingsByDate> savingsByDates
) {}
