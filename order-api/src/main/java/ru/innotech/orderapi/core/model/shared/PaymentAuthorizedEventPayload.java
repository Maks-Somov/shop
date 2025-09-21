package ru.innotech.orderapi.core.model.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizedEventPayload {
    private String orderId;
}
