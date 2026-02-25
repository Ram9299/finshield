package com.finshield.controller;

import com.finshield.dto.CreateTransactionRequest;
import com.finshield.dto.CreateTransactionResponse;
import com.finshield.dto.RiskDetailsResponse;
import com.finshield.service.RiskQueryService;
import com.finshield.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final RiskQueryService riskQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTransactionResponse create(@Valid @RequestBody CreateTransactionRequest req) {
        return transactionService.create(req);
    }

    @GetMapping("/{id}/risk")
    public RiskDetailsResponse risk(@PathVariable("id") UUID txnId) {
        return riskQueryService.getRisk(txnId);
    }
}