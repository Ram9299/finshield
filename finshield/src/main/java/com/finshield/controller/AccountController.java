package com.finshield.controller;

import com.finshield.dto.AccountResponse;
import com.finshield.dto.CreateAccountRequest;
import com.finshield.dto.CreateAccountResponse;
import com.finshield.service.AccountService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/accounts")
public class AccountController {

  private final AccountService accountService;

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public CreateAccountResponse create(@Valid @RequestBody CreateAccountRequest req) {
    return accountService.create(req);
  }

  @GetMapping("/{id}")
  public AccountResponse get(@PathVariable UUID id) {
    var acc = accountService.getOrThrow(id);
    return new AccountResponse(
        acc.getId(),
        acc.getUser().getId(),
        acc.getBalance(),
        acc.getStatus().name(),
        acc.getCreatedAt());
  }
}
