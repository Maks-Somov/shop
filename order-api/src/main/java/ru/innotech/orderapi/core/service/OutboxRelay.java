package ru.innotech.orderapi.core.service;

import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.innotech.orderapi.adapters.repository.OutboxRepository;
import ru.innotech.orderapi.core.model.OutboxMessage;
import ru.innotech.orderapi.core.model.OutboxStatus;

@Slf4j
@Service
@RequiredArgsConstructor
public class OutboxRelay {
    private final OutboxRepository outboxRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Transactional
    @Scheduled(fixedDelayString = "PT10S")
    public void publishBatch() {
        List<OutboxMessage> batch = outboxRepository.pickBatch(PageRequest.of(0, 100));
        for (OutboxMessage m : batch) {
            try {
                kafkaTemplate.send(m.getTopic(), m.getKey(), m.getPayloadJson());
                log.info("Message {} was sent", m.getPayloadJson());
                m.setStatus(OutboxStatus.SENT);
                m.setLastAttemptAt(Instant.now());
            } catch (Exception ex) {
                m.setStatus(OutboxStatus.ERROR);
                m.setLastAttemptAt(Instant.now());
                m.setLastError(ex.getMessage());
            }
        }
    }
}