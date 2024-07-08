package com.eric6166.order.config.kafka;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
@Setter
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaProducerProps {

    @Value("${spring.kafka.producers.order-created.topic-name}")
    private String orderCreatedTopicName;

}
