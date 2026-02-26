package com.finshield.entity;

import com.finshield.entity.enums.AlertStatus;
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
@Table(name = "alerts")
public class Alert {

  @Id @GeneratedValue private UUID id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "account_id", nullable = false)
  private Account account;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "transaction_id", nullable = false)
  private Transaction transaction;

  @Column(name = "alert_type", nullable = false, length = 40)
  private String alertType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private AlertStatus status;

  @Column(name = "created_at", nullable = false)
  private OffsetDateTime createdAt;
}
