
package com.kafka.microservices.orderservice.controller;

import com.kafka.microservices.orderservice.dto.CreateOrderRequest;
import com.kafka.microservices.orderservice.dto.CreateOrderResponse;
import com.kafka.microservices.orderservice.service.OrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class OrderController {

    private static final Logger log = LoggerFactory.getLogger(OrderController.class);
    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    public ResponseEntity<CreateOrderResponse> createOrder(@RequestBody CreateOrderRequest request){
        CreateOrderResponse response = orderService.createOrder(request);
        log.info("Returning response for order with id: {}", response.orderId());
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

}
