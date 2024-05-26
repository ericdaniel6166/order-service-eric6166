package com.eric6166.order.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Data
@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
    List<Item> itemList;
//    Long addressId;
//    PaymentDetail paymentDetail; //improve later

    @Data
    @FieldDefaults(level = AccessLevel.PRIVATE)
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        Long productId;
        Integer orderQuantity;
    }
}
