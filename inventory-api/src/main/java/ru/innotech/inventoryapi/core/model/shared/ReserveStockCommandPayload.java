package ru.innotech.inventoryapi.core.model.shared;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReserveStockCommandPayload {
    private String orderId;
    private List<String> itemIds;
}