package ru.innotech.orderapi.core.model;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import java.util.UUID;
import lombok.Getter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Document(collection = "events")
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "eventType"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = OrderCreatedEvent.class, name = "OrderCreated"),
        @JsonSubTypes.Type(value = OrderStatusChangedEvent.class, name = "OrderStatusChanged"),
        @JsonSubTypes.Type(value = ItemAddedToOrderEvent.class, name = "ItemAddedToOrder")
})
public abstract class Event {
    @Id
    private String id;
    private final String orderId;
    private final long timestamp;
    private final String eventType;

    protected Event(String orderId, String eventType) {
        this.id = UUID.randomUUID().toString();
        this.orderId = orderId;
        this.eventType = eventType;
        this.timestamp = System.currentTimeMillis();
    }

    public void setId(String id) {
        this.id = id;
    }
}