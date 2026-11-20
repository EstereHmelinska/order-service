package com.kafka.microservices.orderservice.dto;

import java.util.UUID;

public record CreateOrderResponse(
        UUID orderId,
        Long customerId,
        String product,
        int quantity) {
}
