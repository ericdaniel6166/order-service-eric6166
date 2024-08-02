package com.eric6166.order.utils;

import com.eric6166.base.utils.DateTimeUtils;
import com.eric6166.common.config.kafka.AppEvent;
import com.eric6166.order.dto.InventoryReservedEventPayload;
import com.eric6166.order.dto.InventoryReservedFailedEventPayload;
import com.eric6166.order.dto.OrderCreatedEventPayload;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.OrderResponse;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;
import org.apache.commons.lang3.RandomUtils;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.apache.kafka.common.TopicPartition;
import org.springframework.kafka.support.SendResult;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class TestUtils {

    private TestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static InventoryReservedEventPayload mockInventoryReservedEventPayload(String username, InventoryReservedEventPayload.Item... inventoryReservedItems) {
        return InventoryReservedEventPayload.builder()
                .orderDate(DateTimeUtils.toString(LocalDateTime.now(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER))
                .username(username)
                .itemList(List.of(inventoryReservedItems))
                .build();
    }

    public static OrderCreatedEventPayload.Item mockOrderCreatedItem(OrderRequest.Item item) {
        return OrderCreatedEventPayload.Item.builder()
                .productId(item.getProductId())
                .orderQuantity(item.getOrderQuantity())
                .build();
    }

    public static OrderRequest mockOrderRequest(OrderRequest.Item... items) {
        return OrderRequest.builder()
                .itemList(List.of(items))
                .build();
    }

    public static OrderRequest.Item mockOrderRequestItem(Long productId, Integer orderQuantity) {
        return OrderRequest.Item.builder()
                .productId(productId)
                .orderQuantity(orderQuantity)
                .build();

    }

    public static Order mockOrder(String username, OrderStatus orderStatus, String orderDetail, BigDecimal totalAmount) {
        return Order.builder()
                .orderId(Order.OrderId.builder()
                        .username(username)
                        .orderDate(LocalDateTime.now())
                        .orderStatusValue(orderStatus.getValue())
                        .build())
                .orderDetail(orderDetail)
                .totalAmount(totalAmount)
                .build();
    }

    public static OrderResponse mockOrderResponse(Order order) {
        return OrderResponse.builder()
                .orderDate(order.getOrderId().getOrderDate())
                .orderStatus(OrderStatus.fromValue(order.getOrderId().getOrderStatusValue()).name())
                .totalAmount(order.getTotalAmount())
                .build();

    }

    public static InventoryReservedEventPayload.Item mockInventoryReservedItem(OrderRequest.Item item, BigDecimal productPrice) {
        return InventoryReservedEventPayload.Item.builder()
                .orderQuantity(item.getOrderQuantity())
                .productId(item.getProductId())
                .productPrice(productPrice)
                .build();
    }

    public static InventoryReservedFailedEventPayload.Item mockInventoryReservedFailedItem(OrderRequest.Item item, Integer inventoryQuantity) {
        return InventoryReservedFailedEventPayload.Item.builder()
                .productId(item.getProductId())
                .orderQuantity(item.getOrderQuantity())
                .inventoryQuantity(inventoryQuantity)
                .build();
    }

    public static OrderDto mockOrderDto(Order order) {
        return OrderDto.builder()
                .totalAmount(order.getTotalAmount())
                .orderDate(order.getOrderId().getOrderDate())
                .orderDetail(order.getOrderDetail())
                .username(order.getOrderId().getUsername())
                .orderStatusValue(order.getOrderId().getOrderStatusValue())
                .build();
    }

    public static CompletableFuture<SendResult<String, Object>> mockSendResultSuccess(String topicName, AppEvent appEvent) {
        return CompletableFuture.completedFuture(new SendResult<>(
                new ProducerRecord<>(topicName, appEvent),
                new RecordMetadata(
                        new TopicPartition(topicName, RandomUtils.nextInt()),
                        RandomUtils.nextLong(),
                        RandomUtils.nextInt(),
                        RandomUtils.nextLong(),
                        RandomUtils.nextInt(),
                        RandomUtils.nextInt())));
    }
}
