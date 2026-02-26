package com.finshield.fraud.rules;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.FraudSignalType;
import com.finshield.fraud.FraudRule;
import com.finshield.repository.TransactionRepository;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GeoAnomalyRule implements FraudRule {

  private final TransactionRepository transactionRepository;

  @Override
  public Optional<FraudSignal> evaluate(Transaction txn) {
    return transactionRepository
        .findTopByAccount_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
            txn.getAccount().getId(), txn.getCreatedAt())
        .flatMap(
            prev -> {
              boolean countryChanged = !prev.getCountry().equalsIgnoreCase(txn.getCountry());
              long minutes =
                  Math.abs(Duration.between(prev.getCreatedAt(), txn.getCreatedAt()).toMinutes());

              if (countryChanged && minutes <= 10) {
                return Optional.of(
                    FraudSignal.builder()
                        .transaction(txn)
                        .signalType(FraudSignalType.GEO_ANOMALY)
                        .riskWeight(25)
                        .details(
                            "Country changed from "
                                + prev.getCountry()
                                + " to "
                                + txn.getCountry()
                                + " within "
                                + minutes
                                + " minutes")
                        .createdAt(OffsetDateTime.now())
                        .build());
              }
              return Optional.empty();
            });
  }
}
