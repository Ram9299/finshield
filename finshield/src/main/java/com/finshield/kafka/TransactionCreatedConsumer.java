package com.finshield.kafka;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.enums.RiskDecision;
import com.finshield.kafka.events.TransactionCreatedEvent;
import com.finshield.obs.FraudMetrics;
import com.finshield.repository.FraudSignalRepository;
import com.finshield.repository.TransactionRepository;
import com.finshield.service.AlertService;
import com.finshield.service.FraudDetectionService;
import com.finshield.service.RiskScoringService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionCreatedConsumer {

  private final TransactionRepository transactionRepository;
  private final FraudDetectionService fraudDetectionService;
  private final FraudSignalRepository fraudSignalRepository;
  private final RiskScoringService riskScoringService;
  private final AlertService alertService;
  private final FraudMetrics metrics;

  @KafkaListener(topics = "${finshield.kafka.topic.txn-created}", groupId = "finshield-fraud")
  public void onMessage(TransactionCreatedEvent event) {

    // ✅ Metric: txn consumed/processed
    metrics.incTxnCreated();

    var txn =
        transactionRepository
            .findById(event.transactionId())
            .orElseThrow(
                () -> new IllegalStateException("Transaction not found: " + event.transactionId()));

    // Run fraud rules
    List<FraudSignal> signals = fraudDetectionService.detect(txn);

    // ✅ Metric: each signal type
    for (FraudSignal s : signals) {
      metrics.incSignal(s.getSignalType().name());
    }

    if (!signals.isEmpty()) {
      fraudSignalRepository.saveAll(signals);
    }

    int totalScore = signals.stream().mapToInt(FraudSignal::getRiskWeight).sum();
    var risk = riskScoringService.saveScore(txn, totalScore);

    // ✅ Metric: decision type (SAFE/REVIEW/BLOCK)
    metrics.incDecision(risk.getDecision().name());

    if (risk.getDecision() == RiskDecision.BLOCK) {
      alertService.triggerFraudAlert(txn);

      // ✅ Metric: alerts created
      metrics.incAlertsCreated();
    }
  }
}
