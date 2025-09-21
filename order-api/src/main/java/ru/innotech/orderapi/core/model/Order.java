package ru.innotech.orderapi.core.model;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "orders")
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String orderId;
    private String customerId;
    private String status;
    private BigDecimal amount;

    @Transient
    private boolean isPaymentPending;
    @Transient
    private boolean isConfirmed;
    @Transient
    private boolean isCancelled;

    @ElementCollection
    @CollectionTable(name = "order_items", joinColumns = @JoinColumn(name = "order_id"))
    private List<String> items = new ArrayList<>();

    public Order(String orderId) {
        this.orderId = orderId;
    }

    public void applyEvent(Event event) {
        if (event instanceof OrderCreatedEvent) {
            OrderCreatedEvent e = (OrderCreatedEvent) event;
            this.customerId = e.getCustomerId();
            this.status = "CREATED";  // Статус по умолчанию
        } else if (event instanceof OrderStatusChangedEvent) {
            OrderStatusChangedEvent e = (OrderStatusChangedEvent) event;
            this.status = e.getStatus();
            if (e.getStatus().equals("PAYMENT_PENDING")) {
                this.amount = e.getAmount();
                this.isPaymentPending = true;
            }
            if (e.getStatus().equals("CONFIRMED")) {
                this.isConfirmed = true;
            }
            if (e.getStatus().equals("CANCELLED")) {
                this.isCancelled = true;
            }
        } else if (event instanceof ItemAddedToOrderEvent) {
            ItemAddedToOrderEvent e = (ItemAddedToOrderEvent) event;
            this.items.add(e.getItemId());
        }
    }

    public void replayEvents(List<Event> events) {
        events.forEach(this::applyEvent);
    }

}