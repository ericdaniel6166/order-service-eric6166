package com.eric6166.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@AllArgsConstructor
@NoArgsConstructor
@Data
public class OrderCreatedEventPayload {
    private String username;
    private List<Item> itemList;
    private String orderDate;

    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    @Data
    public static class Item {
        private Long productId;
        private Integer orderQuantity;
    }
}
