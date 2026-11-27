package com.kafka.microservices.orderservice.service;

import com.kafka.microservices.orderservice.dto.CreateOrderRequest;
import com.kafka.microservices.orderservice.dto.OrderCreatedEvent;
import com.kafka.microservices.orderservice.producer.OrderEventProducer;
import com.kafka.microservices.orderservice.entity.Order;
import com.kafka.microservices.orderservice.entity.OrderStatus;
import com.kafka.microservices.orderservice.repository.OrderRepository;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.dao.DataAccessResourceFailureException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderEventProducer orderEventProducer;

    @Mock
    private OrderRepository orderRepository;

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
    void createOrder_shouldPublishOrderCreatedEvent() {
        //Arrange
        //The producer is mocked, so no Kafka broker is needed
        //Act
        orderService.createOrder(laptopRequest);

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
        orderService.createOrder(laptopRequest);

        //Assert
        verify(orderEventProducer).publishOrderCreatedEvent(eventCaptor.capture());

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

    @Test
    void createOrder_shouldThrowExceptionWhenProductIsNull() {
        // Arrange
        CreateOrderRequest invalidRequest =
                new CreateOrderRequest(customerId, null, quantity);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(invalidRequest)
        );

        verify(orderEventProducer, never())
                .publishOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrder_shouldThrowExceptionWhenQuantityIsZero() {
        // Arrange
        CreateOrderRequest invalidRequest =
                new CreateOrderRequest(customerId, product, 0);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderService.createOrder(invalidRequest)
        );

        verify(orderEventProducer, never())
                .publishOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrder_shouldSaveOrderWithCorrectDetails() {
        // Arrange
        ArgumentCaptor<Order> orderCaptor =
                ArgumentCaptor.forClass(Order.class);

        // Act
        orderService.createOrder(laptopRequest);

        // Verify
        verify(orderRepository).save(orderCaptor.capture());

        Order savedOrder = orderCaptor.getValue();

        // Assert
        assertNotNull(savedOrder.getId());
        assertEquals(customerId, savedOrder.getCustomerId());
        assertEquals(product, savedOrder.getProduct());
        assertEquals(quantity, savedOrder.getQuantity());
        assertEquals(OrderStatus.NEW, savedOrder.getStatus());
        assertNotNull(savedOrder.getCreatedAt());
        assertInstanceOf(UUID.class, savedOrder.getId());
        assertInstanceOf(LocalDateTime.class, savedOrder.getCreatedAt());
    }

    @Test
    void createOrder_shouldSaveOrderBeforePublishingEvent() {
        // Arrange
        var inOrder = inOrder(orderRepository, orderEventProducer);

        // Act
        orderService.createOrder(laptopRequest);

        // Verify
        inOrder.verify(orderRepository).save(any(Order.class));
        inOrder.verify(orderEventProducer)
                .publishOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @Test
    void createOrder_shouldNotPublishEventWhenDatabaseSaveFails() {
        // Arrange
        when(orderRepository.save(any(Order.class)))
                .thenThrow(new DataAccessResourceFailureException(
                        "Database connection failed"));

        // Act & Assert
        assertThrows(DataAccessResourceFailureException.class,
                () -> orderService.createOrder(laptopRequest));

        //Verify
        verify(orderRepository, times(1)).save(any(Order.class));

        verify(orderEventProducer, never())
                .publishOrderCreatedEvent(any(OrderCreatedEvent.class));
    }

    @ParameterizedTest
    @EnumSource(value = OrderStatus.class, names = {"AVAILABLE", "OUT_OF_STOCK"})
    void updateOrderStatus_shouldUpdateAndSaveOrder(OrderStatus newStatus) {
        // Arrange
        UUID orderId = UUID.randomUUID();

        Order order = Order.builder()
                .id(orderId)
                .customerId(customerId)
                .product(product)
                .quantity(quantity)
                .status(OrderStatus.NEW)
                .createdAt(LocalDateTime.now())
                .build();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.of(order));

        ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);

        // Act
        orderService.updateOrderStatus(orderId, newStatus);

        // Verify
        verify(orderRepository).save(orderCaptor.capture());

        Order updatedOrder = orderCaptor.getValue();

        // Assert
        assertEquals(orderId, updatedOrder.getId());
        assertEquals(newStatus, updatedOrder.getStatus());
    }

    @Test
    void updateOrderStatus_shouldThrowExceptionWhenOrderDoesNotExist() {
        // Arrange
        UUID orderId = UUID.randomUUID();

        when(orderRepository.findById(orderId))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                orderService.updateOrderStatus(
                        orderId,
                        OrderStatus.AVAILABLE));

        // Verify
        verify(orderRepository, never()).save(any(Order.class));
    }

}