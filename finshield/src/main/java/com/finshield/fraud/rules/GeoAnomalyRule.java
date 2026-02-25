package com.finshield.fraud.rules;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.FraudSignalType;
import com.finshield.fraud.FraudRule;
import com.finshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class GeoAnomalyRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Optional<FraudSignal> evaluate(Transaction txn) {
        return transactionRepository.findTopByAccount_IdOrderByCreatedAtDesc(txn.getAccount().getId())
                .flatMap(last -> {
                    // If this "last" is the same txn (rare due to timing), ignore
                    if (last.getId() != null && last.getId().equals(txn.getId())) return Optional.empty();

                    boolean countryChanged = !last.getCountry().equalsIgnoreCase(txn.getCountry());
                    long minutes = Math.abs(Duration.between(last.getCreatedAt(), txn.getCreatedAt()).toMinutes());

                    // Country changed within 10 minutes -> risk 25
                    if (countryChanged && minutes <= 10) {
                        return Optional.of(FraudSignal.builder()
                                .transaction(txn)
                                .signalType(FraudSignalType.GEO_ANOMALY)
                                .riskWeight(25)
                                .details("Country changed from " + last.getCountry() + " to " + txn.getCountry()
                                        + " within " + minutes + " minutes")
                                .createdAt(OffsetDateTime.now())
                                .build());
                    }
                    return Optional.empty();
                });
    }
}