package com.eric6166.order.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Optional;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum OrderStatus {
    PLACE_ORDER(0),
    INVENTORY_CHECKED(1),
    ITEM_NOT_AVAILABLE(2),
    ;

    Integer orderStatusValue;

    public static OrderStatus fromValue(Integer value) {
        for (OrderStatus orderStatus : OrderStatus.class.getEnumConstants()) {
            if (orderStatus.orderStatusValue.equals(value))
                return orderStatus;
        }
        throw new IllegalArgumentException(String.format("value '%s' is not a valid OrderStatus value", value));
    }

    public static Optional<OrderStatus> fromValueOptional(Integer value) {
        try {
            return Optional.of(fromValue(value));
        } catch (IllegalArgumentException e) {
            return Optional.empty();
        }
    }

}
