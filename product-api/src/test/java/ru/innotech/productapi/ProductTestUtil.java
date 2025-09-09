package ru.innotech.productapi;

import java.math.BigDecimal;
import lombok.experimental.UtilityClass;
import ru.innotech.productapi.adapters.controller.dto.request.ProductRequest;
import ru.innotech.productapi.adapters.controller.dto.response.ProductResponse;
import ru.innotech.productapi.core.model.Product;
import ru.innotech.productapi.core.model.ProductStatus;

@UtilityClass
public class ProductTestUtil {
    public static Product product1Mock() {
        return Product.builder()
                .name("Product1")
                .description("Description of product1")
                .price(BigDecimal.valueOf(100))
                .currency("RUB")
                .discount(BigDecimal.ZERO)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    public static Product product2Mock() {
        return Product.builder()
                .name("Product2")
                .description("Description of product2")
                .price(BigDecimal.valueOf(200))
                .currency("RUB")
                .discount(BigDecimal.ZERO)
                .status(ProductStatus.ACTIVE)
                .build();
    }

    public static ProductResponse product1ResponseMock(Long id) {
        return new ProductResponse(
                id,
                "Product1",
                "Description of product1",
                BigDecimal.valueOf(100),
                "RUB",
                BigDecimal.ZERO);
    }

    public static ProductResponse product2ResponseMock(Long id) {
        return new ProductResponse(
                id,
                "Product2",
                "Description of product2",
                BigDecimal.valueOf(200),
                "RUB",
                BigDecimal.ZERO);
    }

    public static ProductRequest productCreateRequest() {
        return new ProductRequest(
                "Created product",
                "Created product description",
                BigDecimal.valueOf(56),
                "RUB"
        );
    }

    public static ProductRequest productUpdateRequest() {
        return new ProductRequest(
                "Updated product",
                "Updated product description",
                BigDecimal.valueOf(87),
                "RUB"
        );
    }
}
