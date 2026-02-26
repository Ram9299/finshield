package com.finshield.entity;

import com.finshield.entity.enums.RiskDecision;
import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "risk_scores")
public class RiskScore {

  @Id @GeneratedValue private UUID id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transaction_id", nullable = false, unique = true)
  private Transaction transaction;

  @Column(name = "total_score", nullable = false)
  private Integer totalScore;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private RiskDecision decision;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
