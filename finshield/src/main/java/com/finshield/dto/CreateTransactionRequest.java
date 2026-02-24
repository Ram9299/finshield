package com.finshield.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateTransactionRequest(
        @NotNull UUID accountId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotBlank String txnType,            // DEBIT / CREDIT
        @NotBlank @Size(min = 2, max = 2) String country,
        @NotBlank @Size(max = 120) String deviceId,
        @NotBlank @Size(max = 45) String ipAddress
) {}