package com.finshield.service;

import com.finshield.dto.CreateTransactionRequest;
import com.finshield.dto.CreateTransactionResponse;
import com.finshield.entity.Account;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.TxnType;
import com.finshield.kafka.TransactionEventPublisher;
import com.finshield.kafka.events.TransactionCreatedEvent;
import com.finshield.repository.TransactionRepository;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final AccountService accountService;

  private final TransactionEventPublisher eventPublisher;

  public CreateTransactionResponse create(CreateTransactionRequest req) {
    Account account = accountService.getOrThrow(req.accountId());

    TxnType type;
    try {
      type = TxnType.valueOf(req.txnType().toUpperCase());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid txnType. Use DEBIT or CREDIT");
    }

    Transaction txn =
        Transaction.builder()
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

    // ✅ Async fraud processing via Kafka
    eventPublisher.publish(
        new TransactionCreatedEvent(
            savedTxn.getId(), savedTxn.getAccount().getId(), savedTxn.getCreatedAt()));

    return new CreateTransactionResponse(
        savedTxn.getId(),
        savedTxn.getAccount().getId(),
        savedTxn.getAmount(),
        savedTxn.getCurrency(),
        savedTxn.getTxnType().name(),
        savedTxn.getCountry(),
        savedTxn.getDeviceId(),
        savedTxn.getIpAddress(),
        savedTxn.getCreatedAt());
  }
}
