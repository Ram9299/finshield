package com.finshield.controller;

import com.finshield.dto.CreateTransactionRequest;
import com.finshield.dto.CreateTransactionResponse;
import com.finshield.dto.RiskDetailsResponse;
import com.finshield.dto.RecentTransactionItem;
import com.finshield.service.TransactionQueryService;
import com.finshield.service.RiskQueryService;
import com.finshield.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final RiskQueryService riskQueryService;
    private final TransactionQueryService transactionQueryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CreateTransactionResponse create(@Valid @RequestBody CreateTransactionRequest req) {
        return transactionService.create(req);
    }

    @GetMapping("/{id}/risk")
    public RiskDetailsResponse risk(@PathVariable("id") UUID txnId) {
        return riskQueryService.getRisk(txnId);
    }

    @GetMapping("/recent")
    public List<RecentTransactionItem> recent(@RequestParam(defaultValue = "50") int limit) {
        return transactionQueryService.recent(limit);
    }
}