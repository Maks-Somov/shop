package ru.innotech.orderapi.core.model.shared;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentAuthorizeCommandPayload {
    private String orderId;
    private BigDecimal amount;
}