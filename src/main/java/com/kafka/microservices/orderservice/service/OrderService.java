package com.kafka.microservices.orderservice.service;

import com.kafka.microservices.orderservice.dto.CreateOrderRequest;
import com.kafka.microservices.orderservice.dto.CreateOrderResponse;
import com.kafka.microservices.orderservice.dto.OrderCreatedEvent;
import com.kafka.microservices.orderservice.producer.OrderEventProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);
    private final OrderEventProducer orderEventProducer;

    public OrderService(OrderEventProducer orderEventProducer) {
        this.orderEventProducer = orderEventProducer;
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request){
        if (request.quantity() <= 0 || request.product() == null) {
            log.warn("Invalid order request received");
            throw new IllegalArgumentException("Invalid order details");
        }
        UUID orderId = UUID.randomUUID();
        LocalDateTime createdTime = LocalDateTime.now();
        log.info("Creating a new order for customer with id: {}", request.customerId());
        OrderCreatedEvent orderCreatedEvent = new OrderCreatedEvent(
                orderId,
                request.customerId(),
                request.product(),
                request.quantity(),
                createdTime);
        orderEventProducer.publishOrderCreatedEvent(orderCreatedEvent);
        log.info("Order created successfully with id: {}", orderId);
        return new CreateOrderResponse(orderId, request.customerId(), request.product(), request.quantity());
    }

}
