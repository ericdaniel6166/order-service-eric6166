package com.eric6166.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class InventoryReservedFailedEventPayload {
    private String orderUuid;
    private String username;
    private List<Item> itemList;
    private String orderDate;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Item {
        private Long productId;
        private Integer orderQuantity;
        private Integer inventoryQuantity;
    }
}
