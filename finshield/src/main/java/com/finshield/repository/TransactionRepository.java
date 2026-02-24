package com.finshield.repository;

import com.finshield.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.OffsetDateTime;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    long countByAccount_IdAndCreatedAtAfter(UUID accountId, OffsetDateTime after);
}