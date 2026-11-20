
package com.kafka.microservices.orderservice.producer;

import com.kafka.microservices.orderservice.dto.OrderCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderEventProducer {

    private static final Logger log = LoggerFactory.getLogger(OrderEventProducer.class);
    private static final String TOPIC = "order-created";
    private final KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate;

    public OrderEventProducer(KafkaTemplate<String, OrderCreatedEvent> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishOrderCreatedEvent(OrderCreatedEvent orderCreatedEvent){

        kafkaTemplate.send(TOPIC, orderCreatedEvent.orderId().toString(), orderCreatedEvent)
                .whenComplete((result, exception)->{
                    if(exception != null){
                        log.error("Failed to publish order event {}", orderCreatedEvent.orderId(), exception);
                    } else{
                        log.info("Successfully published order {} to Topic {} | Partition: {} | Offset: {}",
                                orderCreatedEvent.orderId(),
                                result.getRecordMetadata().topic(),
                                result.getRecordMetadata().partition(),
                                result.getRecordMetadata().offset());
                    }
                });
    }

}
