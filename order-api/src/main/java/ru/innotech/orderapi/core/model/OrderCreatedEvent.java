package ru.innotech.orderapi.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class OrderCreatedEvent extends Event {
    private final String customerId;

    @JsonCreator
    public OrderCreatedEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("customerId") String customerId) {
        super(orderId, "OrderCreated");
        this.customerId = customerId;
    }

}
