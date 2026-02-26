package com.finshield.sim;

import com.finshield.sim.dto.SimulationStatusResponse;
import com.finshield.sim.dto.StartSimulationRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sim")
public class SimulationController {

  private final TransactionSimulatorService simulator;

  @PostMapping("/start")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public SimulationStatusResponse start(@Valid @RequestBody StartSimulationRequest req) {
    simulator.start(
        req.accountId(),
        req.tps(),
        req.fraudRatePercent(),
        req.enableRapidTxn(),
        req.enableGeoAnomaly(),
        req.enableAmountSpike(),
        req.enableDeviceMismatch());
    return status();
  }

  @PostMapping("/stop")
  public SimulationStatusResponse stop() {
    simulator.stop();
    return status();
  }

  @GetMapping("/status")
  public SimulationStatusResponse status() {
    var s = simulator.getState();
    return new SimulationStatusResponse(
        s.running.get(),
        s.accountId,
        s.tps,
        s.fraudRatePercent,
        s.enableRapidTxn,
        s.enableGeoAnomaly,
        s.enableAmountSpike,
        s.enableDeviceMismatch);
  }
}
