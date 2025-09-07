package ru.innotech.productapi.core.mapper;

import org.mapstruct.Mapper;
import ru.innotech.productapi.adapters.controller.dto.request.ProductRequest;
import ru.innotech.productapi.adapters.controller.dto.response.ProductResponse;
import ru.innotech.productapi.core.model.Product;

@Mapper(componentModel = "spring")
public interface ProductMapper {
    ProductResponse toDto(Product product);
    Product toEntity(ProductRequest dto);
}
