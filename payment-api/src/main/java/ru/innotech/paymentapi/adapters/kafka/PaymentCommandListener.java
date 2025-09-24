package ru.innotech.paymentapi.adapters.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.paymentapi.adapters.repository.PaymentRepository;
import ru.innotech.paymentapi.core.model.Payment;
import ru.innotech.paymentapi.core.model.shared.MessageEnvelope;
import ru.innotech.paymentapi.core.model.shared.PaymentAuthorizedEventPayload;
import ru.innotech.paymentapi.core.model.shared.PaymentCancelledEventPayload;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentCommandListener {

    private final PaymentRepository paymentRepository;
    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${topics.payment.replies}")
    private String paymentReplies;

    @KafkaListener(topics = "${topics.payment.commands}")
    @Transactional
    public void onCommands(String raw) throws JsonProcessingException {
        log.info("Message {} was received", raw);
        JsonNode node = mapper.readTree(raw);
        String type = node.get("type").asText();

        if ("PaymentAuthorizeCommand".equals(type)) {
            JsonNode payload = node.get("payload");
            String orderId = payload.get("orderId").asText();
            BigDecimal amount = payload.get("amount").decimalValue();

            Payment p = paymentRepository.findByOrderId(orderId).orElseGet(Payment::new);
            p.setOrderId(orderId);
            p.setAmount(amount);

            p.setStatus("AUTHORIZED");
            paymentRepository.save(p);

            MessageEnvelope<PaymentAuthorizedEventPayload> evt = MessageEnvelope.<PaymentAuthorizedEventPayload>builder()
                    .messageId(UUID.randomUUID().toString())
                    .sagaId(node.get("sagaId").asText(null))
                    .orderId(orderId)
                    .type("PaymentAuthorizedEvent")
                    .timestamp(Instant.now())
                    .payload(new PaymentAuthorizedEventPayload(orderId))
                    .build();

            kafkaTemplate.send(paymentReplies, orderId, mapper.writeValueAsString(evt));
            log.info("Message {} was sent", mapper.writeValueAsString(evt));
        }

        if ("PaymentCancelCommand".equals(type)) {
            JsonNode payload = node.get("payload");
            String orderId = payload.get("orderId").asText();
            Payment p = paymentRepository.findByOrderId(orderId).orElseGet(Payment::new);
            p.setOrderId(orderId);
            p.setStatus("CANCELLED");
            paymentRepository.save(p);

            MessageEnvelope<PaymentCancelledEventPayload> evt = MessageEnvelope.<PaymentCancelledEventPayload>builder()
                    .messageId(UUID.randomUUID().toString())
                    .sagaId(node.get("sagaId").asText(null))
                    .orderId(orderId)
                    .type("PaymentCancelledEvent")
                    .timestamp(Instant.now())
                    .payload(new PaymentCancelledEventPayload(orderId))
                    .build();

            kafkaTemplate.send(paymentReplies, orderId, mapper.writeValueAsString(evt));
            log.info("Message {} was sent", mapper.writeValueAsString(evt));
        }
    }
}
