package ru.innotech.orderorchestrator.core.model.shared;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageEnvelope<T> {
    private String messageId;
    private String sagaId;
    private String orderId;
    private String type;
    private Instant timestamp;
    private T payload;
}