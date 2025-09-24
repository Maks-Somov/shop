package ru.innotech.orderapi.adapters.kafka;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.orderapi.adapters.repository.EventStoreRepository;
import ru.innotech.orderapi.adapters.repository.OrderRepository;
import ru.innotech.orderapi.core.model.Event;
import ru.innotech.orderapi.core.model.Order;
import ru.innotech.orderapi.core.model.OrderStatusChangedEvent;
import ru.innotech.orderapi.core.service.OutboxService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventStoreRepository eventStoreRepository;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;

    @Transactional
    @KafkaListener(topics = "${topics.orders.input}")
    public void consumeEvent(ConsumerRecord<String, Event> eventRecord) {
        Event event = eventRecord.value();

        eventStoreRepository.save(event);

        Order order = getOrderFromEvents(event.getOrderId());
        order.applyEvent(event);

        handleEvent(event, order);
    }

    private Order getOrderFromEvents(String orderId) {
        List<Event> events = eventStoreRepository.findByOrderId(orderId);

        Order order = new Order(orderId);
        order.replayEvents(events);
        return order;
    }

    private void handleEvent(Event event, Order order) {
        if (event instanceof OrderStatusChangedEvent statusChangedEvent) {
            if (isPaymentPending(statusChangedEvent)) {
                outboxService.enqueueOrder(order);
            }
            if (isCancelled(statusChangedEvent) || isConfirmed(statusChangedEvent)) {
                orderRepository.save(order);
            }
        }
    }

    private boolean isPaymentPending(OrderStatusChangedEvent event) {
        return event.getStatus().equals("PAYMENT_PENDING");
    }

    private boolean isConfirmed(OrderStatusChangedEvent event) {
        return event.getStatus().equals("CONFIRMED");
    }

    private boolean isCancelled(OrderStatusChangedEvent event) {
        return event.getStatus().equals("CANCELLED");
    }
}
