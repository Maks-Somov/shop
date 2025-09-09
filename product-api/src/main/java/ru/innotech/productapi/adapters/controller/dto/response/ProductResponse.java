package ru.innotech.productapi.adapters.controller.dto.response;

import java.math.BigDecimal;

public record ProductResponse(
        Long id,
        String name,
        String description,
        BigDecimal price,
        String currency,
        BigDecimal discount
) {
}
