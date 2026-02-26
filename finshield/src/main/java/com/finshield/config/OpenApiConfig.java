package com.finshield.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
  @Bean
  public OpenAPI finshieldOpenAPI() {
    return new OpenAPI()
        .info(
            new Info()
                .title("FinShield – Fraud Pattern Simulator API")
                .version("1.0")
                .description("Transaction simulation + fraud rules + risk scoring + alerts"));
  }
}
