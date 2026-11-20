package com.kafka.microservices.orderservice.dto;

public record CreateOrderRequest (
        Long customerId,
        String product,
        int quantity) {
}
