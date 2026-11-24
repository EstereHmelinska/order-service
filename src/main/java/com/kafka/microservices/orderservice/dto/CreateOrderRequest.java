package com.kafka.microservices.orderservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CreateOrderRequest (
        Long customerId,
        @NotNull
        String product,
        @Positive
        int quantity) {
}
