package ru.innotech.orderapi.adapters.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.innotech.orderapi.adapters.kafka.EventProducer;
import ru.innotech.orderapi.adapters.repository.OrderRepository;
import ru.innotech.orderapi.core.model.Event;
import ru.innotech.orderapi.core.model.Order;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {
    private final EventProducer eventProducer;
    private final OrderRepository orderRepository;

    @PostMapping
    public void sendEvent(@RequestBody Event event) {
        eventProducer.sendEvent(event);
    }

    @GetMapping("/{id}")
    public Order getOrder(@PathVariable String id) {
        return orderRepository.findByOrderId(id);
    }
}
