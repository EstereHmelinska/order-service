package com.kafka.microservices.orderservice.producer;

import com.kafka.microservices.orderservice.dto.OrderCreatedEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    private static final String TOPIC = "order-created";

    @Mock
    private KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    @InjectMocks
    private OrderEventProducer orderEventProducer;

    private OrderCreatedEvent event;

    @BeforeEach
    void setUp() {
        event = new OrderCreatedEvent(
                UUID.randomUUID(),
                15L,
                "Laptop",
                2,
                LocalDateTime.now()
        );
    }

    @Test
    void publishOrderCreatedEvent_shouldSendEventToCorrectTopic() {
        // Arrange
        RecordMetadata metadata = new RecordMetadata(
                new TopicPartition(TOPIC, 0),
                1L,
                0,
                System.currentTimeMillis(),
                0,
                0
        );

        SendResult<String, OrderCreatedEvent> sendResult =
                new SendResult<>(null, metadata);

        when(kafkaTemplate.send(TOPIC, event.orderId().toString(), event))
                .thenReturn(CompletableFuture.completedFuture(sendResult));

        //Act
        orderEventProducer.publishOrderCreatedEvent(event);

        //Assert
        verify(kafkaTemplate, times(1))
                .send(TOPIC, event.orderId().toString(), event);
    }

    @Test
    void publishOrderCreatedEvent_shouldHandlePublishingFailure() {
        //Arrange
        CompletableFuture<SendResult<String, OrderCreatedEvent>> failedFuture =
                CompletableFuture.failedFuture(
                        new Exception("Kafka publishing failed")
                );

        when(kafkaTemplate.send(TOPIC, event.orderId().toString(), event))
                .thenReturn(failedFuture);

        //Act & Assert
        assertDoesNotThrow(() ->
                orderEventProducer.publishOrderCreatedEvent(event)
        );

        verify(kafkaTemplate, times(1))
                .send(TOPIC, event.orderId().toString(), event);
    }
}