package com.eric6166.order.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderCreatedEventPayload {
    String orderUuid;
    String username;
    List<Item> itemList;
    String orderDate;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    public static class Item {
        Long productId;
        Integer orderQuantity;
    }
}
