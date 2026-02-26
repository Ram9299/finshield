package com.finshield;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class FraudFlowIT {

  @Container
  static PostgreSQLContainer<?> postgres =
      new PostgreSQLContainer<>("postgres:16")
          .withDatabaseName("finshield")
          .withUsername("postgres")
          .withPassword("postgres");

  @Container
  static GenericContainer<?> redis = new GenericContainer<>("redis:7").withExposedPorts(6379);

  @Container
  static KafkaContainer kafka =
      new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.1"));

  @DynamicPropertySource
  static void props(DynamicPropertyRegistry r) {
    // Postgres
    r.add("spring.datasource.url", postgres::getJdbcUrl);
    r.add("spring.datasource.username", postgres::getUsername);
    r.add("spring.datasource.password", postgres::getPassword);
    r.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
    r.add("spring.flyway.enabled", () -> true);

    // Redis
    r.add("spring.data.redis.host", redis::getHost);
    r.add("spring.data.redis.port", () -> redis.getMappedPort(6379));

    // Kafka
    r.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
  }

  @Autowired TestRestTemplate rest;

  @Test
  void asyncFlow_shouldEventuallyBlockAndCreateAlert() {
    UUID accountId = createUserAndAccount();

    // Build normal history
    for (int i = 0; i < 6; i++) {
      post("/api/transactions", txn(accountId, bd("1000"), "IN", "device-1", "49.204.12.10"));
    }

    // Spike
    Map<String, Object> resp =
        post(
            "/api/transactions",
            txn(accountId, bd("100000"), "IN", "device-unknown", "49.204.12.10"));

    UUID txnId = uuid(resp, "id");

    // Poll until READY
    Map<String, Object> risk = waitUntilReady(txnId);

    assertEquals("READY", risk.get("status"));
    assertEquals("BLOCK", risk.get("decision"));

    List<?> alerts = getList("/api/alerts?status=OPEN");
    assertFalse(alerts.isEmpty());
  }

  // ---- polling helper ----

  private Map<String, Object> waitUntilReady(UUID txnId) {
    Instant deadline = Instant.now().plus(Duration.ofSeconds(15));

    while (Instant.now().isBefore(deadline)) {
      Map<String, Object> risk = getMap("/api/transactions/" + txnId + "/risk");

      if ("READY".equals(risk.get("status"))) {
        return risk;
      }

      sleep(300);
    }

    fail("Risk never became READY within timeout");
    return Map.of();
  }

  private void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException ignored) {
    }
  }

  // ---- helpers ----

  private UUID createUserAndAccount() {
    String email = "test_" + UUID.randomUUID() + "@example.com";

    Map<String, Object> user =
        post(
            "/api/users",
            Map.of(
                "fullName", "Test User",
                "email", email,
                "country", "IN"));

    UUID userId = uuid(user, "id");

    Map<String, Object> acc = post("/api/accounts", Map.of("userId", userId.toString()));

    return uuid(acc, "id");
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

    assertTrue(res.getStatusCode().is2xxSuccessful());
    return (Map<String, Object>) res.getBody();
  }

  @SuppressWarnings("unchecked")
  private Map<String, Object> getMap(String path) {
    ResponseEntity<Map> res = rest.getForEntity(path, Map.class);
    assertEquals(200, res.getStatusCode().value());
    return (Map<String, Object>) res.getBody();
  }

  private List<?> getList(String path) {
    ResponseEntity<List> res = rest.getForEntity(path, List.class);
    return Objects.requireNonNullElse(res.getBody(), List.of());
  }

  private UUID uuid(Map<String, Object> map, String key) {
    return UUID.fromString(String.valueOf(map.get(key)));
  }
}
