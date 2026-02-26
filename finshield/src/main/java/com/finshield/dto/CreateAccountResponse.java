package com.finshield.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateAccountResponse(UUID id, UUID userId, BigDecimal balance, String status) {}
