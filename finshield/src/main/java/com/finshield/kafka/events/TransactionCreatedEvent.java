package com.finshield.kafka.events;

import java.time.OffsetDateTime;
import java.util.UUID;

public record TransactionCreatedEvent(
    UUID transactionId, UUID accountId, OffsetDateTime createdAt) {}
