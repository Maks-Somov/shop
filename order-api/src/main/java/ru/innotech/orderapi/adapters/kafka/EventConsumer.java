package ru.innotech.orderapi.adapters.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
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
import ru.innotech.orderapi.core.model.OrderCreatedEvent;
import ru.innotech.orderapi.core.model.OrderStatusChangedEvent;
import ru.innotech.orderapi.core.model.shared.MessageEnvelope;
import ru.innotech.orderapi.core.model.shared.OrderConfirmRequestedEvent;
import ru.innotech.orderapi.core.service.OutboxService;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventConsumer {

    private final EventStoreRepository eventStoreRepository;
    private final OrderRepository orderRepository;
    private final OutboxService outboxService;
    private final ObjectMapper objectMapper;

    @Transactional
    @KafkaListener(topics = "${topics.orders.input}")
    public void consumeEvent(ConsumerRecord<String, Event> eventRecord) throws JsonProcessingException {
        log.info("Message {} was received", objectMapper.writeValueAsString(eventRecord.value()));
        Event event = eventRecord.value();

        eventStoreRepository.save(event);

        Order order = getOrderFromEvents(event.getOrderId());
        order.applyEvent(event);

        if (order.isPaymentPending()) {
            outboxService.enqueueOrder(order);
        }
        if (order.isCancelled() || order.isConfirmed()) {
            orderRepository.save(order);
        }
    }

    private Order getOrderFromEvents(String orderId) {
        List<Event> events = eventStoreRepository.findByOrderId(orderId);

        Order order = new Order(orderId);
        order.replayEvents(events);
        return order;
    }
}
