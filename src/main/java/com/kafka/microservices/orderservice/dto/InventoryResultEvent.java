package com.kafka.microservices.orderservice.dto;

import com.kafka.microservices.orderservice.entity.OrderStatus;

import java.util.UUID;

/**
 *
 * Represents the message received from Inventory Service
 */
public record InventoryResultEvent(
        UUID orderId,
        OrderStatus status) {}