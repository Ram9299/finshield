package com.finshield.service;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.fraud.FraudRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FraudDetectionService {

    private final List<FraudRule> rules; // Spring injects all @Component rules

    public List<FraudSignal> detect(Transaction txn) {
        List<FraudSignal> signals = new ArrayList<>();
        for (FraudRule rule : rules) {
            rule.evaluate(txn).ifPresent(signals::add);
        }
        return signals;
    }
}