package com.finshield.fraud.rules;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.FraudSignalType;
import com.finshield.fraud.FraudRule;
import com.finshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class AmountSpikeRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Optional<FraudSignal> evaluate(Transaction txn) {
        // Compare against average of last 30 days
        Double avg = transactionRepository.avgAmountSince(
                txn.getAccount().getId(),
                OffsetDateTime.now().minusDays(30)
        );

        if (avg == null || avg <= 0) return Optional.empty(); // not enough history

        BigDecimal avgBd = BigDecimal.valueOf(avg);
        BigDecimal threshold = avgBd.multiply(BigDecimal.valueOf(5)); // 5x spike

        if (txn.getAmount().compareTo(threshold) > 0) {
            return Optional.of(FraudSignal.builder()
                    .transaction(txn)
                    .signalType(FraudSignalType.AMOUNT_SPIKE)
                    .riskWeight(20)
                    .details("Amount " + txn.getAmount() + " is > 5x avg (" + avgBd + ")")
                    .createdAt(OffsetDateTime.now())
                    .build());
        }

        return Optional.empty();
    }
}