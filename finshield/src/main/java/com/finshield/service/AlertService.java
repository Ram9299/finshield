package com.finshield.service;

import com.finshield.entity.Alert;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.AlertStatus;
import com.finshield.repository.AlertRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AlertService {

  private final AlertRepository alertRepository;

  public void triggerFraudAlert(Transaction txn) {
    Alert alert =
        Alert.builder()
            .account(txn.getAccount())
            .transaction(txn)
            .alertType("FRAUD_SUSPECTED")
            .status(AlertStatus.OPEN)
            .createdAt(OffsetDateTime.now())
            .build();

    alertRepository.save(alert);
  }
}
