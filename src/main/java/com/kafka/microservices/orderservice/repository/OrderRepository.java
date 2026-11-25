package com.kafka.microservices.orderservice.repository;

import com.kafka.microservices.orderservice.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderRepository extends JpaRepository<Order, UUID> {
}
