package com.eric6166.order.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ItemNotAvailableEventPayload {
    String orderUuid;
    String username;
    List<Item> itemList;
    String orderDate;

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        Long productId;
        Integer orderQuantity;
        Integer inventoryQuantity;
    }
}
