package com.finshield.sim.dto;

import java.util.UUID;

public record SimulationStatusResponse(
        boolean running,
        UUID accountId,
        int tps,
        int fraudRatePercent,
        boolean enableRapidTxn,
        boolean enableGeoAnomaly,
        boolean enableAmountSpike,
        boolean enableDeviceMismatch
) {}