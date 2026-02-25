package com.finshield.fraud.rules;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.FraudSignalType;
import com.finshield.fraud.FraudRule;
import com.finshield.fraud.RedisSlidingWindowCounter;
import com.finshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RapidTransactionRule implements FraudRule {

    private final RedisSlidingWindowCounter counter;
    private final TransactionRepository transactionRepository; // fallback

    private static final Duration WINDOW = Duration.ofSeconds(60);
    private static final int THRESHOLD = 5; // >=5 txns in 60s
    private static final int WEIGHT = 30;

    @Override
    public Optional<FraudSignal> evaluate(Transaction txn) {
        String key = "rapid:" + txn.getAccount().getId();
        long count;

        try {
            count = counter.addAndCount(key, System.currentTimeMillis(), WINDOW);
        } catch (Exception e) {
            // Redis down? fallback to DB method so system still works
            count = transactionRepository.countByAccount_IdAndCreatedAtAfter(
                    txn.getAccount().getId(),
                    OffsetDateTime.now().minusSeconds(60)
            );
        }

        if (count >= THRESHOLD) {
            return Optional.of(FraudSignal.builder()
                    .transaction(txn)
                    .signalType(FraudSignalType.RAPID_TXN)
                    .riskWeight(WEIGHT)
                    .details("Txn count in last 60s: " + count)
                    .createdAt(OffsetDateTime.now())
                    .build());
        }
        return Optional.empty();
    }
}