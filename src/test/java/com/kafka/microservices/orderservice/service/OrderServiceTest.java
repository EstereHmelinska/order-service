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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderEventProducer orderEventProducer;

    @InjectMocks
    private OrderService orderService;

    private CreateOrderRequest request;

    @BeforeEach
    void setUp() {
        request = new CreateOrderRequest(15L, "Laptop", 2);
    }

    @Test
    void createOrder_shouldPublishOrderCreatedEvent() {
        //Arrange
        //The producer is mocked, so no Kafka broker is needed
        //Act
        orderService.createOrder(request);

        //Assert
        verify(orderEventProducer, times(1))
                .publishOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrder_shouldPublishEventWithCorrectDetails() {
        //Arrange
        ArgumentCaptor<OrderCreatedEvent> eventCaptor =
                ArgumentCaptor.forClass(OrderCreatedEvent.class);

        //Act
        orderService.createOrder(request);

        //Assert
        verify(orderEventProducer).publishOrderCreatedEvent(eventCaptor.capture());

        OrderCreatedEvent publishedEvent = eventCaptor.getValue();

        assertNotNull(publishedEvent.orderId());
        assertInstanceOf(UUID.class, publishedEvent.orderId());
        assertEquals(15L, publishedEvent.customerId());
        assertEquals("Laptop", publishedEvent.product());
        assertEquals(2, publishedEvent.quantity());
        assertNotNull(publishedEvent.createdTime());
        assertInstanceOf(LocalDateTime.class, publishedEvent.createdTime());
    }
}