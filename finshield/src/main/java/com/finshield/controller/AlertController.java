package com.finshield.controller;

import com.finshield.dto.AlertItem;
import com.finshield.entity.Alert;
import com.finshield.entity.enums.AlertStatus;
import com.finshield.exception.NotFoundException;
import com.finshield.repository.AlertRepository;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alerts")
public class AlertController {

  private final AlertRepository alertRepository;

  @GetMapping
  public List<AlertItem> getAlerts(@RequestParam(defaultValue = "OPEN") String status) {
    AlertStatus st;
    try {
      st = AlertStatus.valueOf(status.toUpperCase());
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid status. Use OPEN or CLOSED");
    }

    return alertRepository.findByStatusOrderByCreatedAtDesc(st).stream()
        .map(
            a ->
                new AlertItem(
                    a.getId(),
                    a.getAccount().getId(),
                    a.getTransaction().getId(),
                    a.getAlertType(),
                    a.getStatus().name(),
                    a.getCreatedAt()))
        .toList();
  }

  @PostMapping("/{id}/close")
  public String close(@PathVariable UUID id) {

    Alert alert =
        alertRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Alert not found: " + id));

    alert.setStatus(AlertStatus.CLOSED);
    alertRepository.save(alert);

    return "Alert closed successfully";
  }
}
