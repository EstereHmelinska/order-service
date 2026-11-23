package com.kafka.microservices.orderservice.controller;

import tools.jackson.databind.ObjectMapper;
import com.kafka.microservices.orderservice.dto.CreateOrderRequest;
import com.kafka.microservices.orderservice.dto.CreateOrderResponse;
import com.kafka.microservices.orderservice.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.argThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private CreateOrderRequest laptopRequest;
    private CreateOrderResponse laptopResponse;
    private static final Long customerId = 15L;
    private static final String product = "Laptop";
    private static final int quantity = 2;

    @BeforeEach
    void setUp() {
        UUID orderId = UUID.randomUUID();

        laptopRequest = new CreateOrderRequest(customerId, product, quantity);
        laptopResponse = new CreateOrderResponse(orderId, customerId, product, quantity);
    }

    @Test
    void createOrder_shouldReturnCorrectResponseBody() throws Exception {
        //Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(laptopResponse);

        //Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(laptopRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(laptopResponse.orderId().toString()))
                .andExpect(jsonPath("$.customerId").value(customerId))
                .andExpect(jsonPath("$.product").value(product))
                .andExpect(jsonPath("$.quantity").value(quantity));
    }

    @Test
    void createOrder_shouldCallOrderServiceWithCorrectRequest() throws Exception {
        //Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(laptopResponse);

        //Act
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(laptopRequest)));

        //Assert
        verify(orderService, times(1)).createOrder(
                argThat(orderRequest ->
                        orderRequest.customerId().equals(customerId)
                                && orderRequest.product().equals(product)
                                && orderRequest.quantity() == quantity
                )
        );
    }

    @Test
    void createOrder_shouldReturnInternalServerErrorWhenServiceFails() throws Exception {
        // Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenThrow(new IllegalStateException("Order creation failed"));

        // Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(laptopRequest)))
                .andExpect(status().isInternalServerError());
    }
}