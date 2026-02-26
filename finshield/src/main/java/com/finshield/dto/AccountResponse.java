package com.finshield.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record AccountResponse(
    UUID id, UUID userId, BigDecimal balance, String status, OffsetDateTime createdAt) {}
