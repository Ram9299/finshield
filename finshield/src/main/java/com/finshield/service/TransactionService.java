package com.finshield.service;

import com.finshield.dto.CreateTransactionRequest;
import com.finshield.dto.CreateTransactionResponse;
import com.finshield.entity.Account;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.TxnType;
import com.finshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

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

        Transaction saved = transactionRepository.save(txn);

        return new CreateTransactionResponse(
                saved.getId(),
                saved.getAccount().getId(),
                saved.getAmount(),
                saved.getCurrency(),
                saved.getTxnType().name(),
                saved.getCountry(),
                saved.getDeviceId(),
                saved.getIpAddress(),
                saved.getCreatedAt()
        );
    }
}