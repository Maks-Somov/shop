package ru.innotech.orderapi.adapters.controller.dto;

public record EventDto(
        String orderId,
        String customerId
) {
}
