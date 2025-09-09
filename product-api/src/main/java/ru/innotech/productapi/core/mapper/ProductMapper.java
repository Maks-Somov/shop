package ru.innotech.productapi.core.mapper;

import java.math.BigDecimal;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.innotech.productapi.adapters.controller.dto.request.ProductRequest;
import ru.innotech.productapi.adapters.controller.dto.response.ProductResponse;
import ru.innotech.productapi.core.model.Product;
import ru.innotech.productapi.core.model.ProductStatus;

@Mapper(componentModel = "spring",
        uses = {},
        imports = {BigDecimal.class, ProductStatus.class},
        unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
    ProductResponse toDto(Product product);

    Product toEntity(ProductRequest productRequest);
}
