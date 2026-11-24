package com.kafka.microservices.orderservice.service;

import com.kafka.microservices.orderservice.dto.CreateOrderRequest;
import com.kafka.microservices.orderservice.dto.OrderCreatedEvent;
import com.kafka.microservices.orderservice.producer.OrderEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest laptopRequest;
    private static final Long customerId = 15L;
    private static final String product = "Laptop";
    private static final int quantity = 2;

    @BeforeEach
    void setUp() {
        laptopRequest = new CreateOrderRequest(customerId, product, quantity);
    }

    @Test
    void createOrder_shouldPublishEventWithCorrectDetails() {
        //Arrange
        ArgumentCaptor<OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderCreatedEvent.class);

        //Act
        orderService.createOrder(laptopRequest);

        //Assert
        verify(orderEventProducer, times(1))
                .publishOrderCreatedEvent(eventCaptor.capture());

        OrderCreatedEvent publishedEvent = eventCaptor.getValue();

        assertNotNull(publishedEvent.orderId());
        assertInstanceOf(UUID.class, publishedEvent.orderId());
        assertEquals(customerId, publishedEvent.customerId());
        assertEquals(product, publishedEvent.product());
        assertEquals(quantity, publishedEvent.quantity());
        assertNotNull(publishedEvent.createdTime());
        assertInstanceOf(LocalDateTime.class, publishedEvent.createdTime());
    }

    @Test
    void createOrder_shouldPropagateExceptionWhenEventPublishingFails() {
        // Arrange
        doThrow(new IllegalStateException("Event publishing failed"))
                .when(orderEventProducer)
                .publishOrderCreatedEvent(any(OrderCreatedEvent.class));

        // Act & Assert
        assertThrows(IllegalStateException.class, () ->
                orderService.createOrder(laptopRequest)
        );

        verify(orderEventProducer, times(1))
                .publishOrderCreatedEvent(any(OrderCreatedEvent.class));
    }
}