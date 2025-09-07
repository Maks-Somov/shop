package ru.innotech.productapi.adapters.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.innotech.productapi.core.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByIdIn(List<Long> ids);
}
