package ru.innotech.productapi.adapters.discount.dto;

import java.math.BigDecimal;

public record DiscountResponse(
        Long productId,
        BigDecimal discount
) {
}
