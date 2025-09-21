package ru.innotech.orderorchestrator.core.model;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.Data;

@Data
@Entity
@Table(name = "order_saga")
public class OrderSaga {
    @Id
    private String sagaId;
    private String orderId;
    @Enumerated(EnumType.STRING)
    private SagaState state;
    private Instant deadlineAt;
    private int retries;
    private String lastError;
}