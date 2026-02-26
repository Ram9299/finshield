package com.finshield.sim;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class SimulationState {

  public final AtomicBoolean running = new AtomicBoolean(false);

  public volatile UUID accountId;
  public volatile int tps;
  public volatile int fraudRatePercent;

  public volatile boolean enableRapidTxn;
  public volatile boolean enableGeoAnomaly;
  public volatile boolean enableAmountSpike;
  public volatile boolean enableDeviceMismatch;
}
