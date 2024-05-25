package com.eric6166.order.config.kafka;

import com.eric6166.common.config.kafka.KafkaConsumerConfig;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;

@Configuration
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KafkaConsumerGroupIdConfig {

    KafkaConsumerConfig kafkaConsumerConfig;

    KafkaConsumerProps kafkaConsumerProps;

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> itemNotAvailableKafkaListenerContainerFactory() {
        return kafkaConsumerConfig.kafkaListenerContainerFactory(kafkaConsumerProps.getItemNotAvailableGroupId());
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> inventoryCheckedGroupIdKafkaListenerContainerFactory() {
        return kafkaConsumerConfig.kafkaListenerContainerFactory(kafkaConsumerProps.getInventoryCheckedGroupId());
    }


}
