package com.finshield.sim;

import com.finshield.dto.CreateTransactionRequest;
import com.finshield.service.TransactionService;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransactionSimulatorService {

  private final TransactionService transactionService;

  private final SimulationState state = new SimulationState();
  private final SecureRandom rnd = new SecureRandom();

  // simple “profile” for the simulated user/account
  private volatile String homeCountry = "IN";
  private volatile String primaryDevice = "device-android-1";
  private volatile String primaryIp = "49.204.12.10";
  private volatile String primaryCurrency = "INR";

  private static final List<String> COUNTRIES = List.of("IN", "US", "GB", "SG", "AE", "DE", "JP");
  private static final List<String> IPS =
      List.of(
          "49.204.12.10",
          "49.37.88.21",
          "34.23.11.90",
          "18.222.10.4",
          "103.21.55.2",
          "91.198.174.192",
          "203.0.113.10");

  public void start(
      UUID accountId,
      int tps,
      int fraudRatePercent,
      boolean rapid,
      boolean geo,
      boolean spike,
      boolean deviceMismatch) {
    state.accountId = accountId;
    state.tps = tps;
    state.fraudRatePercent = fraudRatePercent;
    state.enableRapidTxn = rapid;
    state.enableGeoAnomaly = geo;
    state.enableAmountSpike = spike;
    state.enableDeviceMismatch = deviceMismatch;

    // reset profile (optional)
    homeCountry = "IN";
    primaryDevice = "device-android-1";
    primaryIp = "49.204.12.10";
    primaryCurrency = "INR";

    state.running.set(true);
  }

  public void stop() {
    state.running.set(false);
  }

  public SimulationState getState() {
    return state;
  }

  // Runs every 200ms. If tps=10 -> we should generate ~2 txns per tick
  @Scheduled(fixedRate = 200)
  public void tick() {
    if (!state.running.get() || state.accountId == null) return;

    int perSecond = Math.max(1, state.tps);
    int perTick = Math.max(1, (int) Math.round(perSecond / 5.0)); // 5 ticks per sec

    for (int i = 0; i < perTick; i++) {
      boolean shouldFraud = rnd.nextInt(100) < state.fraudRatePercent;
      CreateTransactionRequest req = shouldFraud ? buildFraudTxn() : buildNormalTxn();
      try {
        transactionService.create(req);
      } catch (Exception ignored) {
        // keep simulation running even if one txn fails
      }
    }
  }

  private CreateTransactionRequest buildNormalTxn() {
    BigDecimal amount = BigDecimal.valueOf(100 + rnd.nextInt(4900)); // 100..5000
    return new CreateTransactionRequest(
        state.accountId, amount, primaryCurrency, "DEBIT", homeCountry, primaryDevice, primaryIp);
  }

  private CreateTransactionRequest buildFraudTxn() {
    // pick one fraud mode among enabled toggles
    // if none enabled, fallback to normal
    boolean any =
        state.enableRapidTxn
            || state.enableGeoAnomaly
            || state.enableAmountSpike
            || state.enableDeviceMismatch;
    if (!any) return buildNormalTxn();

    int pick = rnd.nextInt(4);

    // try a few picks so we respect toggles
    for (int attempt = 0; attempt < 6; attempt++) {
      switch (pick) {
        case 0 -> {
          if (state.enableRapidTxn) return fraudRapidTxn();
        }
        case 1 -> {
          if (state.enableGeoAnomaly) return fraudGeoAnomaly();
        }
        case 2 -> {
          if (state.enableAmountSpike) return fraudAmountSpike();
        }
        case 3 -> {
          if (state.enableDeviceMismatch) return fraudNewDevice();
        }
      }
      pick = rnd.nextInt(4);
    }
    return buildNormalTxn();
  }

  // Pattern: rapid-fire doesn’t need special fields; it relies on volume.
  private CreateTransactionRequest fraudRapidTxn() {
    BigDecimal amount = BigDecimal.valueOf(50 + rnd.nextInt(500)); // small but frequent
    return new CreateTransactionRequest(
        state.accountId, amount, primaryCurrency, "DEBIT", homeCountry, primaryDevice, primaryIp);
  }

  // Pattern: impossible travel: flip country (and IP) suddenly
  private CreateTransactionRequest fraudGeoAnomaly() {
    String foreignCountry = randomCountryDifferentFrom(homeCountry);
    String ip = randomIp();
    String currency = foreignCountry.equals("IN") ? "INR" : "USD";

    BigDecimal amount = BigDecimal.valueOf(500 + rnd.nextInt(5000));
    return new CreateTransactionRequest(
        state.accountId, amount, currency, "DEBIT", foreignCountry, primaryDevice, ip);
  }

  // Pattern: amount spike: very high value
  private CreateTransactionRequest fraudAmountSpike() {
    BigDecimal amount = BigDecimal.valueOf(60000 + rnd.nextInt(140000)); // 60k..200k
    return new CreateTransactionRequest(
        state.accountId, amount, primaryCurrency, "DEBIT", homeCountry, primaryDevice, primaryIp);
  }

  // Pattern: new device: device never seen before
  private CreateTransactionRequest fraudNewDevice() {
    String newDevice = "device-unknown-" + (1000 + rnd.nextInt(9000));
    BigDecimal amount = BigDecimal.valueOf(500 + rnd.nextInt(8000));
    return new CreateTransactionRequest(
        state.accountId, amount, primaryCurrency, "DEBIT", homeCountry, newDevice, primaryIp);
  }

  private String randomCountryDifferentFrom(String base) {
    String c;
    do {
      c = COUNTRIES.get(rnd.nextInt(COUNTRIES.size()));
    } while (c.equalsIgnoreCase(base));
    return c;
  }

  private String randomIp() {
    return IPS.get(rnd.nextInt(IPS.size()));
  }
}
