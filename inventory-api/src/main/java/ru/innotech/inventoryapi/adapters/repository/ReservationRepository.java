package ru.innotech.inventoryapi.adapters.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.innotech.inventoryapi.core.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    Optional<Reservation> findByOrderId(String orderId);
}
