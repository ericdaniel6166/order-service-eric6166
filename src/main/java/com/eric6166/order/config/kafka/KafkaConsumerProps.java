package com.eric6166.order.config.kafka;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@FieldDefaults(level = AccessLevel.PRIVATE)
@Getter
@Setter
@ConditionalOnProperty(name = "spring.kafka.enabled", havingValue = "true")
public class KafkaConsumerProps {


    @Value("${spring.kafka.consumers.inventory-reserved-failed.group-id}")
    String inventoryReservedFailedGroupId;

    @Value("${spring.kafka.consumers.inventory-reserved.group-id}")
    String inventoryReservedGroupId;


}
