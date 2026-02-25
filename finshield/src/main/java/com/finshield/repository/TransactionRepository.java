package com.finshield.repository;

import com.finshield.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import org.springframework.data.domain.Pageable;
import java.time.OffsetDateTime;
import java.util.List;
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

    @Query("select t from Transaction t order by t.createdAt desc")
    List<Transaction> findRecent(Pageable pageable);

    Optional<Transaction> findTopByAccount_IdAndCreatedAtLessThanOrderByCreatedAtDesc(
            UUID accountId,
            OffsetDateTime createdAt
    );

}