package com.finshield.service;

import com.finshield.dto.RiskDetailsResponse;
import com.finshield.entity.RiskScore;
import com.finshield.repository.FraudSignalRepository;
import com.finshield.repository.RiskScoreRepository;
import com.finshield.repository.TransactionRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskQueryService {

  private final TransactionRepository transactionRepository;
  private final RiskScoreRepository riskScoreRepository;
  private final FraudSignalRepository fraudSignalRepository;

  public RiskDetailsResponse get(UUID txnId) {

    // ensure transaction exists
    transactionRepository
        .findById(txnId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + txnId));

    RiskScore risk = riskScoreRepository.findByTransaction_Id(txnId).orElse(null);

    if (risk == null) {
      return new RiskDetailsResponse(txnId, "PENDING", null, null, List.of());
    }

    var signals =
        fraudSignalRepository.findByTransaction_Id(txnId).stream()
            .map(
                s ->
                    new RiskDetailsResponse.SignalItem(
                        s.getSignalType().name(), s.getRiskWeight(), s.getDetails()))
            .toList();

    return new RiskDetailsResponse(
        txnId, "READY", risk.getTotalScore(), risk.getDecision().name(), signals);
  }
}
