package com.finshield.fraud.rules;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.FraudSignalType;
import com.finshield.fraud.FraudRule;
import com.finshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RapidTransactionRule implements FraudRule {

    private final TransactionRepository transactionRepository;

    @Override
    public Optional<FraudSignal> evaluate(Transaction txn) {
        // More than 5 transactions in last 1 minute -> risk 30
        long count = transactionRepository.countByAccount_IdAndCreatedAtAfter(
                txn.getAccount().getId(),
                OffsetDateTime.now().minusMinutes(1)
        );

        if (count >= 5) {
            return Optional.of(FraudSignal.builder()
                    .transaction(txn)
                    .signalType(FraudSignalType.RAPID_TXN)
                    .riskWeight(30)
                    .details("Txn count in last 1 minute: " + count)
                    .createdAt(OffsetDateTime.now())
                    .build());
        }
        return Optional.empty();
    }
}