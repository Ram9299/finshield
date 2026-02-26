package com.finshield.kafka;

import com.finshield.kafka.events.TransactionCreatedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TransactionEventPublisher {

  private final KafkaTemplate<String, TransactionCreatedEvent> kafkaTemplate;

  @Value("${finshield.kafka.topic.txn-created}")
  private String txnCreatedTopic;

  public void publish(TransactionCreatedEvent event) {
    kafkaTemplate.send(txnCreatedTopic, event.transactionId().toString(), event);
  }
}
