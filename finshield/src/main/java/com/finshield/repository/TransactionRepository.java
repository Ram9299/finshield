package com.finshield.repository;

import com.finshield.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.OffsetDateTime;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    long countByAccount_IdAndCreatedAtAfter(UUID accountId, OffsetDateTime after);

    Optional<Transaction> findTopByAccount_IdOrderByCreatedAtDesc(UUID accountId);

    @Query("""
                select avg(t.amount)
                from Transaction t
                where t.account.id = :accountId
                  and t.createdAt >= :after
            """)
    Double avgAmountSince(UUID accountId, OffsetDateTime after);

    boolean existsByAccount_IdAndDeviceId(UUID accountId, String deviceId);

    @Query("""
                select count(t) > 0
                from Transaction t
                where t.account.id = :accountId
                  and t.deviceId = :deviceId
                  and t.createdAt < :createdAt
            """)
    boolean wasDeviceSeenBefore(UUID accountId, String deviceId, OffsetDateTime createdAt);



}