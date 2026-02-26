package com.finshield.dto;

import java.util.List;
import java.util.UUID;

public record RiskDetailsResponse(
    UUID transactionId, int totalScore, String decision, List<SignalItem> signals) {
  public record SignalItem(String type, int weight, String details) {}
}
