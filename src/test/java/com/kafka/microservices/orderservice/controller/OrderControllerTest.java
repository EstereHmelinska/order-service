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

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OrderService orderService;

    private CreateOrderRequest request;
    private CreateOrderResponse response;

    @BeforeEach
    void setUp() {
        UUID orderId = UUID.randomUUID();

        request = new CreateOrderRequest(15L, "Laptop", 2);
        response = new CreateOrderResponse(orderId, 15L, "Laptop", 2);
    }

    @Test
    void createOrder_shouldReturn201Created() throws Exception {
        //Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(response);

        //Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrder_shouldReturnCorrectResponseBody() throws Exception {
        //Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(response);

        //Act & Assert
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").value(response.orderId().toString()))
                .andExpect(jsonPath("$.customerId").value(15))
                .andExpect(jsonPath("$.product").value("Laptop"))
                .andExpect(jsonPath("$.quantity").value(2));
    }

    @Test
    void createOrder_shouldCallOrderServiceWithCorrectRequest() throws Exception {
        //Arrange
        when(orderService.createOrder(any(CreateOrderRequest.class)))
                .thenReturn(response);

        //Act
        mockMvc.perform(post("/api/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        //Assert
        verify(orderService, times(1)).createOrder(
                argThat(orderRequest ->
                        orderRequest.customerId().equals(15L)
                                && orderRequest.product().equals("Laptop")
                                && orderRequest.quantity() == 2
                )
        );
    }
}