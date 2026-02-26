package com.finshield.sim.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record StartSimulationRequest(
    @NotNull UUID accountId,

    // transactions per second
    @Min(1) @Max(50) int tps,

    // percentage (0-100) of txns that should be fraudulent
    @Min(0) @Max(100) int fraudRatePercent,

    // toggles
    boolean enableRapidTxn,
    boolean enableGeoAnomaly,
    boolean enableAmountSpike,
    boolean enableDeviceMismatch) {}
