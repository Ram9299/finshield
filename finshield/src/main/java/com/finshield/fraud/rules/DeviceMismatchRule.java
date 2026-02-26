package com.finshield.fraud.rules;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.FraudSignalType;
import com.finshield.fraud.FraudRule;
import com.finshield.repository.TransactionRepository;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceMismatchRule implements FraudRule {

  private final TransactionRepository transactionRepository;

  @Override
  public Optional<FraudSignal> evaluate(Transaction txn) {
    boolean seenBefore =
        transactionRepository.wasDeviceSeenBefore(
            txn.getAccount().getId(), txn.getDeviceId(), txn.getCreatedAt());

    if (!seenBefore) {
      return Optional.of(
          FraudSignal.builder()
              .transaction(txn)
              .signalType(FraudSignalType.DEVICE_MISMATCH)
              .riskWeight(25)
              .details("New device for account: " + txn.getDeviceId())
              .createdAt(OffsetDateTime.now())
              .build());
    }
    return Optional.empty();
  }
}
