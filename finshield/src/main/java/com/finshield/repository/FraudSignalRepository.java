package com.finshield.repository;

import com.finshield.entity.FraudSignal;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FraudSignalRepository extends JpaRepository<FraudSignal, UUID> {
  List<FraudSignal> findByTransaction_Id(UUID transactionId);
}
