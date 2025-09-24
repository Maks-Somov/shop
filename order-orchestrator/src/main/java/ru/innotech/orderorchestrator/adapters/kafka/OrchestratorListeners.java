package ru.innotech.orderorchestrator.adapters.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.orderorchestrator.adapters.repository.OrderSagaRepository;
import ru.innotech.orderorchestrator.core.model.OrderSaga;
import ru.innotech.orderorchestrator.core.model.SagaState;
import ru.innotech.orderorchestrator.core.model.shared.MessageEnvelope;
import ru.innotech.orderorchestrator.core.model.shared.PaymentAuthorizeCommandPayload;
import ru.innotech.orderorchestrator.core.model.shared.PaymentCancelCommandPayload;
import ru.innotech.orderorchestrator.core.model.shared.ReserveStockCommandPayload;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrchestratorListeners {

    private final OrderSagaRepository sagaRepository;
    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${topics.payment.commands}")
    private String paymentCommandsTopic;
    @Value("${topics.inventory.commands}")
    private String inventoryCommandsTopic;
    @Value("${topics.orders.output}")
    private String ordersOutputTopic;

    @Transactional
    @KafkaListener(topics = "${topics.orders.events}")
    public void onOrderEvents(String raw) throws JsonProcessingException {
        log.info("Message {} was received", raw);
        JsonNode node = mapper.readTree(raw);
        String type = node.get("type").asText();
        if (!"OrderConfirmRequestedEvent".equals(type)) return;

        String orderId = node.get("payload").get("orderId").asText();
        BigDecimal amount = BigDecimal.valueOf(node.get("payload").get("amount").asDouble());

        OrderSaga saga = sagaRepository.findByOrderId(orderId).orElseGet(() -> {
            OrderSaga s = new OrderSaga();
            s.setSagaId(UUID.randomUUID().toString());
            s.setOrderId(orderId);
            s.setState(SagaState.NEW);
            return s;
        });
        saga.setState(SagaState.WAIT_PAYMENT);
        saga.setDeadlineAt(Instant.now().plus(Duration.ofMinutes(2)));
        sagaRepository.save(saga);

        var cmd = MessageEnvelope.<PaymentAuthorizeCommandPayload>builder()
                .messageId(UUID.randomUUID().toString())
                .sagaId(saga.getSagaId())
                .orderId(orderId)
                .type("PaymentAuthorizeCommand")
                .timestamp(Instant.now())
                .payload(new PaymentAuthorizeCommandPayload(orderId, amount))
                .build();
        kafkaTemplate.send(paymentCommandsTopic, orderId, mapper.writeValueAsString(cmd));
        log.info("Message {} was sent", mapper.writeValueAsString(cmd));
    }

    @Transactional
    @KafkaListener(topics = "${topics.payment.replies}")
    public void onPaymentReplies(String raw) throws JsonProcessingException {
        JsonNode node = mapper.readTree(raw);
        String type = node.get("type").asText();
        String orderId = node.get("payload").get("orderId").asText();

        OrderSaga saga = sagaRepository.findByOrderId(orderId).orElseThrow();
        if ("PaymentAuthorizedEvent".equals(type) && saga.getState() == SagaState.WAIT_PAYMENT) {

            saga.setState(SagaState.WAIT_STOCK);
            saga.setDeadlineAt(Instant.now().plus(Duration.ofMinutes(2)));
            sagaRepository.save(saga);

            var cmd = MessageEnvelope.<ReserveStockCommandPayload>builder()
                    .messageId(UUID.randomUUID().toString())
                    .sagaId(saga.getSagaId())
                    .orderId(orderId)
                    .type("ReserveStockCommand")
                    .timestamp(Instant.now())
                    .payload(new ReserveStockCommandPayload(orderId, List.of()))
                    .build();
            kafkaTemplate.send(inventoryCommandsTopic, orderId, mapper.writeValueAsString(cmd));
        }
        if ("PaymentCancelledEvent".equals(type)) {
            cancelOrder(orderId, saga, "payment failed");
        }
    }

    @Transactional
    @KafkaListener(topics = "${topics.inventory.replies}")
    public void onInventoryReplies(String raw) throws JsonProcessingException {
        log.info("Message {} was received", raw);
        JsonNode node = mapper.readTree(raw);
        String type = node.get("type").asText();
        String orderId = node.get("payload").get("orderId").asText();

        OrderSaga saga = sagaRepository.findByOrderId(orderId).orElseThrow();
        if ("StockReservedEvent".equals(type) && saga.getState() == SagaState.WAIT_STOCK) {
            saga.setState(SagaState.COMPLETED);
            sagaRepository.save(saga);

            kafkaTemplate.send(ordersOutputTopic, orderId, mapper.writeValueAsString(Map.of(
                    "eventType", "OrderStatusChanged",
                    "orderId", orderId,
                    "status", "CONFIRMED"
            )));
            log.info("Message {} was sent", mapper.writeValueAsString(Map.of(
                    "eventType", "OrderStatusChanged",
                    "orderId", orderId,
                    "status", "CONFIRMED"
            )));
        }
        if ("StockReservationFailedEvent".equals(type)) {
            saga.setState(SagaState.ROLLBACKING);
            sagaRepository.save(saga);

            var cancel = MessageEnvelope.<PaymentCancelCommandPayload>builder()
                    .messageId(UUID.randomUUID().toString())
                    .sagaId(saga.getSagaId())
                    .orderId(orderId)
                    .type("PaymentCancelCommand")
                    .timestamp(Instant.now())
                    .payload(new PaymentCancelCommandPayload(orderId))
                    .build();
            kafkaTemplate.send(paymentCommandsTopic, orderId, mapper.writeValueAsString(cancel));
            log.info("Message {} was sent", mapper.writeValueAsString(cancel));
        }
    }

    private void cancelOrder(String orderId, OrderSaga saga, String reason) throws JsonProcessingException {
        saga.setState(SagaState.ROLLED_BACK);
        saga.setLastError(reason);
        sagaRepository.save(saga);

        kafkaTemplate.send(ordersOutputTopic, orderId, mapper.writeValueAsString(Map.of(
                "eventType", "OrderStatusChanged",
                "orderId", orderId,
                "status", "CANCELLED"
        )));
        log.info("Message {} was sent", mapper.writeValueAsString(Map.of(
                "eventType", "OrderStatusChanged",
                "orderId", orderId,
                "status", "CANCELLED"
        )));
    }
}