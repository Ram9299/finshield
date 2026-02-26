package com.finshield.service;

import com.finshield.dto.CreateAccountRequest;
import com.finshield.dto.CreateAccountResponse;
import com.finshield.entity.Account;
import com.finshield.entity.User;
import com.finshield.entity.enums.AccountStatus;
import com.finshield.exception.NotFoundException;
import com.finshield.repository.AccountRepository;
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final UserService userService;

  public CreateAccountResponse create(CreateAccountRequest req) {
    User user = userService.getOrThrow(req.userId());

    Account account =
        Account.builder()
            .user(user)
            .balance(BigDecimal.ZERO)
            .status(AccountStatus.ACTIVE)
            .createdAt(OffsetDateTime.now())
            .build();

    Account saved = accountRepository.save(account);

    return new CreateAccountResponse(
        saved.getId(), saved.getUser().getId(), saved.getBalance(), saved.getStatus().name());
  }

  public Account getOrThrow(UUID accountId) {
    return accountRepository
        .findById(accountId)
        .orElseThrow(() -> new NotFoundException("Account not found: " + accountId));
  }
}
