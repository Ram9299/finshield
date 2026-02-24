package com.finshield.repository;

import com.finshield.entity.Alert;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface AlertRepository extends JpaRepository<Alert, UUID> {
    List<Alert> findByStatusOrderByCreatedAtDesc(com.finshield.entity.enums.AlertStatus status);
}