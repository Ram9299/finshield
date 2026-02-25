package com.finshield.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

public record AlertItem(
        UUID id,
        UUID accountId,
        UUID transactionId,
        String alertType,
        String status,
        OffsetDateTime createdAt
) {}