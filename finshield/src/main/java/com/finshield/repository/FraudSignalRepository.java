package com.finshield.repository;

import com.finshield.entity.FraudSignal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FraudSignalRepository extends JpaRepository<FraudSignal, UUID> {
    List<FraudSignal> findByTransaction_Id(UUID transactionId);
}