package com.finshield.repository;

import com.finshield.entity.Alert;
import com.finshield.entity.enums.AlertStatus;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AlertRepository extends JpaRepository<Alert, UUID> {

  List<Alert> findByStatusOrderByCreatedAtDesc(AlertStatus status);

  Optional<Alert> findById(UUID id);
}
