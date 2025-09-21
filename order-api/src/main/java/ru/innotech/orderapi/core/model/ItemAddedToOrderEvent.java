package ru.innotech.orderapi.core.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;

@Getter
public class ItemAddedToOrderEvent extends Event {
    private final String itemId;
    private final int quantity;

    @JsonCreator
    public ItemAddedToOrderEvent(
            @JsonProperty("orderId") String orderId,
            @JsonProperty("itemId") String itemId,
            @JsonProperty("quantity") int quantity) {
        super(orderId, "ItemAddedToOrder");
        this.itemId = itemId;
        this.quantity = quantity;
    }
}