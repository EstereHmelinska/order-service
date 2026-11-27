package com.kafka.microservices.orderservice.consumer;

import com.kafka.microservices.orderservice.dto.InventoryResultEvent;
import com.kafka.microservices.orderservice.entity.OrderStatus;
import com.kafka.microservices.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;

@ExtendWith({MockitoExtension.class, OutputCaptureExtension.class})
class InventoryResultConsumerTest {

    @Mock
    private OrderService orderService;

    private InventoryResultConsumer inventoryResultConsumer;

    @BeforeEach
    void setUp() {
        inventoryResultConsumer = new InventoryResultConsumer(orderService);
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"AVAILABLE", "OUT_OF_STOCK"})
    void consumeInventoryResult_shouldUpdateOrderStatus(OrderStatus status, CapturedOutput output) {
        // Arrange
        UUID orderId = UUID.randomUUID();

        InventoryResultEvent event = new InventoryResultEvent(orderId, status);

        // Act
        inventoryResultConsumer.consumeInventoryResult(event);

        // Verify
        verify(orderService).updateOrderStatus(orderId, status);

        // Assert
        assertTrue(output.getOut().contains("Received inventory result for order " + orderId));
        assertTrue(output.getOut().contains(status.toString()));
    }
}
