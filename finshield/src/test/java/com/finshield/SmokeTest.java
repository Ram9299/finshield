package com.finshield;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@Disabled("Disabled because integration tests use Testcontainers for DB/Redis")
@SpringBootTest
class SmokeTest {
    @Test
    void contextLoads() {}
}