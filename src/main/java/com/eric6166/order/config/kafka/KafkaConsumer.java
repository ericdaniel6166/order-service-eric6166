package com.eric6166.order.config.kafka;

import brave.Tracer;
import com.eric6166.common.config.kafka.AppEvent;
import com.eric6166.order.dto.InventoryCheckedEventPayload;
import com.eric6166.order.dto.ItemNotAvailableEventPayload;
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

    @KafkaListener(topics = "${spring.kafka.consumers.item-not-available.topic-name}",
            groupId = "${spring.kafka.consumers.item-not-available.group-id}",
            containerFactory = "itemNotAvailableKafkaListenerContainerFactory",
            concurrency = "${spring.kafka.consumers.item-not-available.properties.concurrency}"
    )
    public void handleItemNotAvailableEvent(AppEvent appEvent) throws JsonProcessingException {
        var span = tracer.nextSpan().name("handleItemNotAvailableEvent").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("ItemNotAvailableEvent uuid", appEvent.getUuid());
            log.info("handleItemNotAvailableEvent, appEvent: {}", appEvent);
            var payload = modelMapper.map(appEvent.getPayload(), ItemNotAvailableEventPayload.class);
            orderService.handleOrderEvent(payload.getOrderUuid(), payload.getUsername(), payload, payload.getOrderDate(), OrderStatus.ITEM_NOT_AVAILABLE, null);
        } catch (RuntimeException e) {
            log.info("e: {} , errorMessage: {}", e.getClass().getName(), e.getMessage()); // comment // for local testing
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }

    }

    @KafkaListener(topics = "${spring.kafka.consumers.inventory-checked.topic-name}",
            groupId = "${spring.kafka.consumers.inventory-checked.group-id}",
            containerFactory = "inventoryCheckedGroupIdKafkaListenerContainerFactory",
            concurrency = "${spring.kafka.consumers.inventory-checked.properties.concurrency}"
    )
    public void handleInventoryCheckedEvent(AppEvent appEvent) throws JsonProcessingException {
        var span = tracer.nextSpan().name("handleInventoryCheckedEvent").start();
        try (var ws = tracer.withSpanInScope(span)) {
            span.tag("InventoryCheckedEvent uuid", appEvent.getUuid());
            log.info("handleInventoryCheckedEvent, appEvent: {}", appEvent);
            var payload = modelMapper.map(appEvent.getPayload(), InventoryCheckedEventPayload.class);
            orderService.handleOrderEvent(payload.getOrderUuid(), payload.getUsername(), payload, payload.getOrderDate(), OrderStatus.INVENTORY_CHECKED, null);
        } catch (RuntimeException e) {
            log.info("e: {} , errorMessage: {}", e.getClass().getName(), e.getMessage()); // comment // for local testing
            span.error(e);
            throw e;
        } finally {
            span.finish();
        }

    }


}
