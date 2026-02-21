package com.blackrock.hackathon.model.response;

import com.blackrock.hackathon.model.SavingsByDate;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public record ReturnsResponse(
        @JsonProperty("transactionsTotalAmount") Long transactionsTotalAmount,
        @JsonProperty("transactionsTotalCeiling") Long transactionsTotalCeiling,
        @JsonProperty("savingsByDates") List<SavingsByDate> savingsByDates
) {}
