package ru.innotech.orderorchestrator.adapters.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.innotech.orderorchestrator.core.model.OrderSaga;

public interface OrderSagaRepository extends JpaRepository<OrderSaga, String> {
    Optional<OrderSaga> findByOrderId(String orderId);
}
