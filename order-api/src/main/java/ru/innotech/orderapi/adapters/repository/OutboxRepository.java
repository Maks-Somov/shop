package ru.innotech.orderapi.adapters.repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import ru.innotech.orderapi.core.model.OutboxMessage;

public interface OutboxRepository extends JpaRepository<OutboxMessage, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select m from OutboxMessage m where m.status = 'NEW' order by m.id asc")
    List<OutboxMessage> pickBatch(Pageable pageable);
}