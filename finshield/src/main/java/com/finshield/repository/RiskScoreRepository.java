package com.finshield.repository;

import com.finshield.entity.RiskScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface RiskScoreRepository extends JpaRepository<RiskScore, UUID> {
    Optional<RiskScore> findByTransaction_Id(UUID transactionId);
}