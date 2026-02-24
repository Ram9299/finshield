package com.finshield.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record CreateTransactionResponse(
        UUID id,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String txnType,
        String country,
        String deviceId,
        String ipAddress,
        OffsetDateTime createdAt
) {}