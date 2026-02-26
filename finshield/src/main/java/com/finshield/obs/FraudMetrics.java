package com.finshield.obs;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.stereotype.Component;

@Component
public class FraudMetrics {

  private final Counter txnCreated;
  private final Counter alertsCreated;

  private final MeterRegistry registry;

  public FraudMetrics(MeterRegistry registry) {
    this.registry = registry;
    this.txnCreated = Counter.builder("finshield.txn.created").register(registry);
    this.alertsCreated = Counter.builder("finshield.alerts.created").register(registry);
  }

  public void incTxnCreated() {
    txnCreated.increment();
  }

  public void incAlertsCreated() {
    alertsCreated.increment();
  }

  public void incDecision(String decision) {
    Counter.builder("finshield.risk.decision")
        .tag("decision", decision)
        .register(registry)
        .increment();
  }

  public void incSignal(String type) {
    Counter.builder("finshield.fraud.signal").tag("type", type).register(registry).increment();
  }
}
