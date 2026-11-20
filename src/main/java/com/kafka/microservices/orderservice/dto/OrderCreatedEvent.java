package com.kafka.microservices.orderservice.dto;

import java.time.LocalDateTime;
import java.util.UUID;

//represents a message that will be published to kafka
public record OrderCreatedEvent(
        UUID orderId,
        Long customerId,
        String product,
        int quantity,
        LocalDateTime createdTime) {
}