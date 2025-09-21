package ru.innotech.paymentapi.adapters.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.innotech.paymentapi.core.model.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByOrderId(String orderId);
}
