package com.eric6166.order.config.kafka;

import com.eric6166.common.config.kafka.KafkaConsumerConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@Configuration
@RequiredArgsConstructor
public class KafkaConsumerGroupIdConfig {

    private final KafkaConsumerConfig kafkaConsumerConfig;

    private final KafkaConsumerProps kafkaConsumerProps;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> inventoryReservedFailedKafkaListenerContainerFactory() {
        return kafkaConsumerConfig.kafkaListenerContainerFactory(kafkaConsumerProps.getInventoryReservedFailedGroupId());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> inventoryReservedGroupIdKafkaListenerContainerFactory() {
        return kafkaConsumerConfig.kafkaListenerContainerFactory(kafkaConsumerProps.getInventoryReservedGroupId());
    }


}
