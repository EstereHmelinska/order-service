package com.kafka.microservices.orderservice.producer;

import com.kafka.microservices.orderservice.dto.OrderCreatedEvent;
import org.apache.kafka.clients.producer.RecordMetadata;
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
import static org.mockito.Mockito.*;

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
        //Arrange
        SendResult<String, OrderCreatedEvent> sendResult = mock(SendResult.class);
        RecordMetadata metadata = mock(RecordMetadata.class);

        when(sendResult.getRecordMetadata()).thenReturn(metadata);
        when(metadata.topic()).thenReturn(TOPIC);
        when(metadata.partition()).thenReturn(0);
        when(metadata.offset()).thenReturn(1L);
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
                        new RuntimeException("Kafka publishing failed")
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