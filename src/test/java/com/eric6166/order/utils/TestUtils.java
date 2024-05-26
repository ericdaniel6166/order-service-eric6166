package com.eric6166.order.utils;

import com.eric6166.order.dto.InventoryCheckedEventPayload;
import com.eric6166.order.dto.ItemNotAvailableEventPayload;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;

import java.math.BigDecimal;

public final class TestUtils {

    private TestUtils() {
        throw new IllegalStateException("Utility class");
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
                .status(orderStatus.name())
                .orderDetail(orderDetail)
                .totalAmount(totalAmount)
                .build();
    }

    public static OrderDto mockOrderDto(Order order, Object orderDetail) {
        return OrderDto.builder()
                .id(order.getId())
                .uuid(order.getUuid())
                .username(order.getUsername())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .orderDetail(orderDetail)
                .build();

    }
    public static InventoryCheckedEventPayload.Item mockInventoryCheckedItem(OrderRequest.Item item, BigDecimal productPrice) {
        return InventoryCheckedEventPayload.Item.builder()
                .orderQuantity(item.getOrderQuantity())
                .productId(item.getProductId())
                .productPrice(productPrice)
                .build();
    }

    public static ItemNotAvailableEventPayload.Item mockNotAvailableItem(OrderRequest.Item item, Integer inventoryQuantity) {
        return ItemNotAvailableEventPayload.Item.builder()
                .productId(item.getProductId())
                .orderQuantity(item.getOrderQuantity())
                .inventoryQuantity(inventoryQuantity)
                .build();
    }
}
