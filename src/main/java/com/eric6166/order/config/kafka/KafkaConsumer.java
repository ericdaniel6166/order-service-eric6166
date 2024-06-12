package com.eric6166.order.config.kafka;

import brave.Tracer;
import com.eric6166.common.config.kafka.AppEvent;
import com.eric6166.order.dto.InventoryReservedEventPayload;
import com.eric6166.order.dto.InventoryReservedFailedEventPayload;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class KafkaConsumer {

    Tracer tracer;
    OrderService orderService;
    ModelMapper modelMapper;

    @KafkaListener(topics = "${spring.kafka.consumers.inventory-reserved-failed.topic-name}",
            groupId = "${spring.kafka.consumers.inventory-reserved-failed.group-id}",
            containerFactory = "inventoryReservedFailedKafkaListenerContainerFactory",
            concurrency = "${spring.kafka.consumers.inventory-reserved-failed.properties.concurrency}"
    )
    public void handleInventoryReservedFailedEvent(AppEvent appEvent) throws JsonProcessingException {
        var span = tracer.nextSpan().name("handleInventoryReservedFailedEvent").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("InventoryReservedFailedEvent uuid", appEvent.getUuid());
            log.info("handleInventoryReservedFailedEvent, appEvent: {}", appEvent);
            var payload = modelMapper.map(appEvent.getPayload(), InventoryReservedFailedEventPayload.class);
            orderService.handleOrderEvent(payload.getOrderUuid(), payload.getUsername(), payload, payload.getOrderDate(), OrderStatus.INVENTORY_RESERVED_FAILED, null);
        } catch (RuntimeException e) {
            log.info("e: {} , errorMessage: {}", e.getClass().getName(), e.getMessage()); // comment // for local testing
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }

    }

    @KafkaListener(topics = "${spring.kafka.consumers.inventory-reserved.topic-name}",
            groupId = "${spring.kafka.consumers.inventory-reserved.group-id}",
            containerFactory = "inventoryReservedGroupIdKafkaListenerContainerFactory",
            concurrency = "${spring.kafka.consumers.inventory-reserved.properties.concurrency}"
    )
    public void handleInventoryReservedEvent(AppEvent appEvent) throws JsonProcessingException {
        var span = tracer.nextSpan().name("handleInventoryReservedEvent").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("InventoryReservedEvent uuid", appEvent.getUuid());
            log.info("handleInventoryReservedEvent, appEvent: {}", appEvent);
            var payload = modelMapper.map(appEvent.getPayload(), InventoryReservedEventPayload.class);
            orderService.handleOrderEvent(payload.getOrderUuid(), payload.getUsername(), payload, payload.getOrderDate(), OrderStatus.INVENTORY_RESERVED, null);
        } catch (RuntimeException e) {
            log.info("e: {} , errorMessage: {}", e.getClass().getName(), e.getMessage()); // comment // for local testing
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }

    }


}
