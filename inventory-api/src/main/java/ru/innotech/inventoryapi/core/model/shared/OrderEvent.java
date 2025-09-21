package ru.innotech.inventoryapi.core.model.shared;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderEvent {
    private String orderId;
    private String type;
} // e.g. OrderCreated, OrderStatusChanged, ItemAdded
