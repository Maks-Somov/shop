package ru.innotech.orderapi.adapters.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import ru.innotech.orderapi.core.model.Event;

@Component
@RequiredArgsConstructor
public class EventProducer {

    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${topics.orders.input}")
    String topic;

    @SneakyThrows
    public void sendEvent(Event event) {
        kafkaTemplate.send(topic, event.getId(), objectMapper.writeValueAsString(event));
    }

}