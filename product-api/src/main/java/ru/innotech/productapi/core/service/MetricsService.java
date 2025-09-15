package ru.innotech.productapi.core.service;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MetricsService {
    private final MeterRegistry meterRegistry;

    private Map<String, Counter> errorsCounter = new ConcurrentHashMap<>();

    public void incrementErrorCounter(String operation) {
        errorsCounter.computeIfAbsent(operation, o -> meterRegistry.counter("service.errors", "operation", o))
                .increment();
    }
}
