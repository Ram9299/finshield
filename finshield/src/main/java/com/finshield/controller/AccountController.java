package com.finshield.controller;

import com.finshield.dto.CreateAccountRequest;
import com.finshield.dto.CreateAccountResponse;
import com.finshield.service.AccountService;
import jakarta.validation.Valid;
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
}