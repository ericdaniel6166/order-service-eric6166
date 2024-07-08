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
public class KafkaConsumerProps {


    @Value("${spring.kafka.consumers.inventory-reserved-failed.group-id}")
    private String inventoryReservedFailedGroupId;

    @Value("${spring.kafka.consumers.inventory-reserved.group-id}")
    private String inventoryReservedGroupId;


}
