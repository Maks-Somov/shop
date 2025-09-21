package ru.innotech.orderapi.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import lombok.Getter;

@Getter
public class OrderStatusChangedEvent extends Event {
    private final String status;
    private final BigDecimal amount;

    @JsonCreator
    public OrderStatusChangedEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("status") String status,
            @JsonProperty("amount") BigDecimal amount) {
        super(orderId, "OrderStatusChanged");
        this.status = status;
        this.amount = amount;
    }

}