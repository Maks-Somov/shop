package ru.innotech.inventoryapi.core.model.shared;

import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderConfirmRequestedEvent {
    private String orderId;
    private BigDecimal amount;
}