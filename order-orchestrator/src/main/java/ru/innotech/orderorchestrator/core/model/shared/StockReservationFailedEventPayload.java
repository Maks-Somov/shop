package ru.innotech.orderorchestrator.core.model.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationFailedEventPayload {
    private String orderId;
    private String reason;
}