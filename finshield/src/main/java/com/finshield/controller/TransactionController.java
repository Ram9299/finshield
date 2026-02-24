package com.finshield.controller;

import com.finshield.dto.CreateTransactionRequest;
import com.finshield.dto.CreateTransactionResponse;
import com.finshield.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTransactionResponse create(@Valid @RequestBody CreateTransactionRequest req) {
        return transactionService.create(req);
    }
}