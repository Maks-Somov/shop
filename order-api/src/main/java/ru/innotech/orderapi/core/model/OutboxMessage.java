package ru.innotech.orderapi.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;

@Entity
@Table(name = "outbox")
@Data
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;
    private String key;
    private String type;
    private String sagaId;
    private String orderId;
    private String messageId;

    @Lob
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    private OutboxStatus status = OutboxStatus.NEW;
    private Instant createdAt = Instant.now();
    private Instant lastAttemptAt;
    private String lastError;
}
