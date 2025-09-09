package ru.innotech.productapi.adapters.controller.dto.request;

import java.math.BigDecimal;

public record ProductRequest(
        String name,
        String description,
        BigDecimal price,
        String currency
) {
}
