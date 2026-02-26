package com.finshield.service;

import com.finshield.dto.RiskDetailsResponse;
import com.finshield.entity.RiskScore;
import com.finshield.exception.NotFoundException;
import com.finshield.repository.FraudSignalRepository;
import com.finshield.repository.RiskScoreRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RiskQueryService {

  private final RiskScoreRepository riskScoreRepository;
  private final FraudSignalRepository fraudSignalRepository;

  public RiskDetailsResponse getRisk(UUID transactionId) {
    RiskScore rs =
        riskScoreRepository
            .findByTransaction_Id(transactionId)
            .orElseThrow(
                () -> new NotFoundException("Risk score not found for txn: " + transactionId));

    var signals =
        fraudSignalRepository.findByTransaction_Id(transactionId).stream()
            .map(
                s ->
                    new RiskDetailsResponse.SignalItem(
                        s.getSignalType().name(), s.getRiskWeight(), s.getDetails()))
            .toList();

    return new RiskDetailsResponse(
        transactionId, rs.getTotalScore(), rs.getDecision().name(), signals);
  }
}
