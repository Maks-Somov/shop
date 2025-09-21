package ru.innotech.orderapi.adapters.repository;

import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import ru.innotech.orderapi.core.model.Event;

public interface EventStoreRepository extends MongoRepository<Event, String> {

    List<Event> findByOrderId(String orderId);
}