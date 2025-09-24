package ru.innotech.inventoryapi.adapters.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.inventoryapi.adapters.repository.ReservationRepository;
import ru.innotech.inventoryapi.core.model.Reservation;
import ru.innotech.inventoryapi.core.model.shared.MessageEnvelope;
import ru.innotech.inventoryapi.core.model.shared.StockReservedEventPayload;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryCommandListener {

    private final ReservationRepository reservationRepository;
    private final ObjectMapper mapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${topics.inventory.replies}")
    private String inventoryReplies;

    @Transactional
    @KafkaListener(topics = "${topics.inventory.commands}")
    public void onCommands(String raw) throws JsonProcessingException {
        JsonNode node = mapper.readTree(raw);
        String type = node.get("type").asText();
        if (!"ReserveStockCommand".equals(type)) return;

        String orderId = node.get("payload").get("orderId").asText();

        Reservation r = reservationRepository.findByOrderId(orderId).orElseGet(Reservation::new);
        r.setOrderId(orderId);
        r.setStatus("RESERVED");
        reservationRepository.save(r);

        MessageEnvelope<StockReservedEventPayload> evt = MessageEnvelope.<StockReservedEventPayload>builder()
                .messageId(UUID.randomUUID().toString())
                .sagaId(node.get("sagaId").asText(null))
                .orderId(orderId)
                .type("StockReservedEvent")
                .timestamp(Instant.now())
                .payload(new StockReservedEventPayload(orderId))
                .build();

        kafkaTemplate.send(inventoryReplies, orderId, mapper.writeValueAsString(evt));
    }
}
