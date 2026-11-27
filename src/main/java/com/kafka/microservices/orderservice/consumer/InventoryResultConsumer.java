package com.kafka.microservices.orderservice.consumer;

import com.kafka.microservices.orderservice.dto.InventoryResultEvent;
import com.kafka.microservices.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryResultConsumer {

    private static final Logger log = LoggerFactory.getLogger(InventoryResultConsumer.class);

    private final OrderService orderService;

    public InventoryResultConsumer(OrderService orderService) {
        this.orderService = orderService;
    }

    @KafkaListener(topics = "inventory-result")
    public void consumeInventoryResult(InventoryResultEvent event) {
        log.info("Received inventory result for order {}: {}",
                event.orderId(),
                event.status());

        orderService.updateOrderStatus(event.orderId(), event.status());
    }
}
