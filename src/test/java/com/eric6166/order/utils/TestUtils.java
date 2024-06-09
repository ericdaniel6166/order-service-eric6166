package com.eric6166.order.utils;

import com.eric6166.base.utils.DateTimeUtils;
import com.eric6166.order.dto.InventoryReservedEventPayload;
import com.eric6166.order.dto.InventoryReservedFailedEventPayload;
import com.eric6166.order.dto.OrderCreatedEventPayload;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public final class TestUtils {

    private TestUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static InventoryReservedEventPayload mockInventoryReservedEventPayload(String uuid, String username, InventoryReservedEventPayload.Item... inventoryReservedItems) {
        return InventoryReservedEventPayload.builder()
                .orderUuid(uuid)
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

    public static Order mockOrder(Long id, String uuid, String username, OrderStatus orderStatus, String orderDetail, BigDecimal totalAmount) {
        return Order.builder()
                .id(id)
                .uuid(uuid)
                .username(username)
                .orderStatusValue(orderStatus.getValue())
                .orderDate(LocalDateTime.now())
                .orderDetail(orderDetail)
                .totalAmount(totalAmount)
                .build();
    }

    public static OrderDto mockOrderDto(Order order, Object orderDetail) {
        return OrderDto.builder()
                .id(order.getId())
                .uuid(order.getUuid())
                .username(order.getUsername())
                .orderStatus(OrderStatus.fromValue(order.getOrderStatusValue()).name())
                .totalAmount(order.getTotalAmount())
                .orderDetail(orderDetail)
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
}
