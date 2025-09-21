package ru.innotech.orderapi.adapters.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.innotech.orderapi.core.model.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {
    Order findByOrderId(String orderId);
}