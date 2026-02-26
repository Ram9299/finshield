package com.finshield.entity;

import com.finshield.entity.enums.FraudSignalType;
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
@Table(name = "fraud_signals")
public class FraudSignal {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  @Enumerated(EnumType.STRING)
  @Column(name = "signal_type", nullable = false, length = 40)
  private FraudSignalType signalType;

  @Column(name = "risk_weight", nullable = false)
  private Integer riskWeight;

  @Column(columnDefinition = "TEXT")
  private String details;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
