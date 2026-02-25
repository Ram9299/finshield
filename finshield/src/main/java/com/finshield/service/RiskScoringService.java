package com.finshield.service;

import com.finshield.entity.RiskScore;
import com.finshield.entity.Transaction;
import com.finshield.entity.enums.RiskDecision;
import com.finshield.repository.RiskScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
public class RiskScoringService {

    private final RiskScoreRepository riskScoreRepository;

    public RiskScore saveScore(Transaction txn, int totalScore) {
        RiskDecision decision = decide(totalScore);

        RiskScore rs = RiskScore.builder()
                .transaction(txn)
                .totalScore(totalScore)
                .decision(decision)
                .createdAt(OffsetDateTime.now())
                .build();

        return riskScoreRepository.save(rs);
    }

    public RiskDecision decide(int score) {
        if (score >= 70) return RiskDecision.BLOCK;
        if (score >= 40) return RiskDecision.REVIEW;
        return RiskDecision.SAFE;
    }
}