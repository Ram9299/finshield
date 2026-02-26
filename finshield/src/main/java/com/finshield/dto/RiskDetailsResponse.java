package com.finshield.dto;

import java.util.List;
import java.util.UUID;

public record RiskDetailsResponse(
    UUID transactionId,
    String status, // PENDING or READY
    Integer totalScore,
    String decision,
    List<SignalItem> signals) {

  public record SignalItem(String type, int riskWeight, String details) {}
}
