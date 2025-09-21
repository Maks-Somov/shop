package ru.innotech.orderapi.core.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.orderapi.adapters.repository.OutboxRepository;
import ru.innotech.orderapi.core.model.Order;
import ru.innotech.orderapi.core.model.OutboxMessage;
import ru.innotech.orderapi.core.model.OutboxStatus;
import ru.innotech.orderapi.core.model.shared.MessageEnvelope;
import ru.innotech.orderapi.core.model.shared.OrderConfirmRequestedEvent;

@Service
@RequiredArgsConstructor
public class OutboxService {
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;

    @Value("${topics.orders.events}")
    private String orderTopic;

    @SneakyThrows
    @Transactional
    public void enqueueOrder(Order order) {
        MessageEnvelope<OrderConfirmRequestedEvent> env = new MessageEnvelope<>(
                UUID.randomUUID().toString(), null, order.getOrderId(), "OrderConfirmRequestedEvent",
                Instant.now(), new OrderConfirmRequestedEvent(order.getOrderId(), order.getAmount())
        );
        OutboxMessage msg = new OutboxMessage();
        msg.setTopic(orderTopic);
        msg.setKey(order.getOrderId());
        msg.setType(order.getClass().getSimpleName());
        msg.setSagaId(env.getSagaId());
        msg.setOrderId(env.getOrderId());
        msg.setMessageId(env.getMessageId());
        msg.setPayloadJson(objectMapper.writeValueAsString(env));
        msg.setStatus(OutboxStatus.NEW);
        msg.setCreatedAt(Instant.now());
        outboxRepository.save(msg);
    }

}

