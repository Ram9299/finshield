package com.finshield.service;

import com.finshield.dto.CreateTransactionRequest;
import com.finshield.dto.CreateTransactionResponse;
import com.finshield.entity.Account;
import com.finshield.entity.FraudSignal;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.RiskDecision;
import com.finshield.entity.enums.TxnType;
import com.finshield.repository.FraudSignalRepository;
import com.finshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final FraudSignalRepository fraudSignalRepository;

    private final AccountService accountService;
    private final FraudDetectionService fraudDetectionService;
    private final RiskScoringService riskScoringService;
    private final AlertService alertService;

    public CreateTransactionResponse create(CreateTransactionRequest req) {
        Account account = accountService.getOrThrow(req.accountId());

        TxnType type;
        try {
            type = TxnType.valueOf(req.txnType().toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid txnType. Use DEBIT or CREDIT");
        }

        Transaction txn = Transaction.builder()
                .account(account)
                .amount(req.amount())
                .currency(req.currency().toUpperCase())
                .txnType(type)
                .country(req.country().toUpperCase())
                .deviceId(req.deviceId())
                .ipAddress(req.ipAddress())
                .createdAt(OffsetDateTime.now())
                .build();

        Transaction savedTxn = transactionRepository.save(txn);

        // Run fraud rules
        List<FraudSignal> signals = fraudDetectionService.detect(savedTxn);
        if (!signals.isEmpty()) {
            fraudSignalRepository.saveAll(signals);
        }

        int totalScore = signals.stream().mapToInt(FraudSignal::getRiskWeight).sum();
        var savedRisk = riskScoringService.saveScore(savedTxn, totalScore);

        if (savedRisk.getDecision() == RiskDecision.BLOCK) {
            alertService.triggerFraudAlert(savedTxn);
        }

        return new CreateTransactionResponse(
                savedTxn.getId(),
                savedTxn.getAccount().getId(),
                savedTxn.getAmount(),
                savedTxn.getCurrency(),
                savedTxn.getTxnType().name(),
                savedTxn.getCountry(),
                savedTxn.getDeviceId(),
                savedTxn.getIpAddress(),
                savedTxn.getCreatedAt()
        );
    }
}