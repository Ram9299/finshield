package com.finshield.fraud;

import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;

import java.util.Optional;

public interface FraudRule {
    Optional<FraudSignal> evaluate(Transaction txn);
}