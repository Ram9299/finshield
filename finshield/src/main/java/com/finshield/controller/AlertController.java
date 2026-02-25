package com.finshield.controller;

import com.finshield.entity.enums.AlertStatus;
import com.finshield.repository.AlertRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertRepository alertRepository;

    @GetMapping
    public List<?> getOpenAlerts(@RequestParam(defaultValue = "OPEN") String status) {
        AlertStatus st;
        try {
            st = AlertStatus.valueOf(status.toUpperCase());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid status. Use OPEN or CLOSED");
        }
        return alertRepository.findByStatusOrderByCreatedAtDesc(st);
    }
}