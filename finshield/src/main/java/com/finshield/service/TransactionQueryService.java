package com.finshield.service;

import com.finshield.dto.RecentTransactionItem;
import com.finshield.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TransactionQueryService {

    private final TransactionRepository transactionRepository;

    public List<RecentTransactionItem> recent(int limit) {
        int safeLimit = Math.min(Math.max(limit, 1), 200);

        return transactionRepository.findRecent(PageRequest.of(0, safeLimit)).stream()
                .map(t -> new RecentTransactionItem(
                        t.getId(),
                        t.getAccount().getId(),
                        t.getAmount(),
                        t.getCurrency(),
                        t.getTxnType().name(),
                        t.getCountry(),
                        t.getDeviceId(),
                        t.getIpAddress(),
                        t.getCreatedAt()
                ))
                .toList();
    }
}