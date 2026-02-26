package com.finshield;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FraudFlowIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("finshield")
          .withUsername("postgres")
          .withPassword("1234");

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    // Postgres
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("spring.flyway.enabled", () -> true);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");

    // Redis (Spring Boot uses spring.data.redis.*)
    r.add("spring.data.redis.host", redis::getHost);
    r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
  }

  @Autowired TestRestTemplate rest;

  @Test
  void shouldCreateAlertWhenRiskIsBlock() {
    UUID accountId = createUserAndAccount();

    // Build some history so AMOUNT_SPIKE can trigger too
    for (int i = 0; i < 6; i++) {
      post(
          "/api/transactions",
          txn(accountId, bd("1000"), "IN", "device-android-1", "49.204.12.10"));
    }

    // With Redis enabled, the RAPID_TXN counter will be hot already.
    // Add a spike + new device to push score >= 70 reliably
    Map<String, Object> spikeResp =
        post(
            "/api/transactions",
            txn(accountId, bd("100000"), "IN", "device-unknown-9999", "49.204.12.10"));

    UUID spikeTxnId = uuid(spikeResp, "id");

    Map<String, Object> risk = getMap("/api/transactions/" + spikeTxnId + "/risk");
    assertEquals("BLOCK", String.valueOf(risk.get("decision")));
    assertTrue(((Number) risk.get("totalScore")).intValue() >= 70);

    List<?> openAlerts = getList("/api/alerts?status=OPEN");
    assertFalse(openAlerts.isEmpty(), "Expected at least one OPEN alert");
  }

  @Test
  void shouldReturnEmptyAlertsWhenNoBlockTransactions() {
    UUID accountId = createUserAndAccount();

    // Only a couple normal txns (avoid rapid threshold)
    for (int i = 0; i < 2; i++) {
      post(
          "/api/transactions", txn(accountId, bd("500"), "IN", "device-android-1", "49.204.12.10"));
    }

    List<?> openAlerts = getList("/api/alerts?status=OPEN");
    assertTrue(openAlerts.isEmpty(), "Expected no OPEN alerts for normal activity");
  }

  @Test
  void rapidTxnShouldAppearInRiskSignals_whenManyTxnsInShortWindow() {
    UUID accountId = createUserAndAccount();

    // Send 6 quick txns to trigger RAPID_TXN via Redis sliding window
    UUID lastTxnId = null;
    for (int i = 0; i < 6; i++) {
      Map<String, Object> resp =
          post(
              "/api/transactions",
              txn(accountId, bd("200"), "IN", "device-android-1", "49.204.12.10"));
      lastTxnId = uuid(resp, "id");
    }
    assertNotNull(lastTxnId);

    Map<String, Object> risk = getMap("/api/transactions/" + lastTxnId + "/risk");

    // signals is a List<Map<String,Object>>
    @SuppressWarnings("unchecked")
    List<Map<String, Object>> signals =
        (List<Map<String, Object>>) risk.getOrDefault("signals", List.of());

    boolean hasRapid =
        signals.stream().anyMatch(s -> "RAPID_TXN".equals(String.valueOf(s.get("type"))));
    assertTrue(hasRapid, "Expected RAPID_TXN signal in risk details when many txns occur quickly");
  }

  // ---------------- helpers ----------------

  private UUID createUserAndAccount() {
    String email = "test_" + UUID.randomUUID() + "@example.com";

    Map<String, Object> userResp =
        post(
            "/api/users",
            Map.of(
                "fullName", "Test User",
                "email", email,
                "country", "IN"));
    UUID userId = uuid(userResp, "id");

    Map<String, Object> accResp = post("/api/accounts", Map.of("userId", userId.toString()));
    return uuid(accResp, "id");
  }

  private Map<String, Object> txn(
      UUID accountId, BigDecimal amount, String country, String deviceId, String ip) {
    Map<String, Object> body = new HashMap<>();
    body.put("accountId", accountId.toString());
    body.put("amount", amount);
    body.put("currency", "INR");
    body.put("txnType", "DEBIT");
    body.put("country", country);
    body.put("deviceId", deviceId);
    body.put("ipAddress", ip);
    return body;
  }

  private BigDecimal bd(String v) {
    return new BigDecimal(v);
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> post(String path, Map<String, ?> body) {
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);

    ResponseEntity<Map> res =
        rest.exchange(path, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);

    assertTrue(
        res.getStatusCode().is2xxSuccessful(),
        "POST " + path + " failed: status=" + res.getStatusCodeValue() + " body=" + res.getBody());

    assertNotNull(res.getBody(), "POST " + path + " returned null body");
    return (Map<String, Object>) res.getBody();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getMap(String path) {
    ResponseEntity<Map> res = rest.getForEntity(path, Map.class);
    assertEquals(200, res.getStatusCode().value(), "GET " + path + " failed");
    assertNotNull(res.getBody(), "GET " + path + " returned null body");
    return (Map<String, Object>) res.getBody();
  }

  private List<?> getList(String path) {
    ResponseEntity<List> res = rest.getForEntity(path, List.class);
    assertEquals(200, res.getStatusCode().value(), "GET " + path + " failed");
    return Objects.requireNonNullElse(res.getBody(), List.of());
  }

  private UUID uuid(Map<String, Object> map, String key) {
    Object v = map.get(key);
    assertNotNull(v, "Missing field: " + key);
    return UUID.fromString(String.valueOf(v));
  }
}
