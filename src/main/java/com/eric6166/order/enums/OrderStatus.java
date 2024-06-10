package com.eric6166.order.enums;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum OrderStatus {
    ORDER_CREATED(0),
    INVENTORY_RESERVED(1),
    INVENTORY_RESERVED_FAILED(2),
    ;

    private static final Map<Integer, OrderStatus> VALUE_MAP =
            Arrays.stream(OrderStatus.values())
                    .collect(Collectors.toMap(OrderStatus::getValue, Function.identity()));

    Integer value;

    public static OrderStatus fromValue(Integer value) {
        var orderStatus = fromValueOptional(value);
        if (orderStatus.isPresent()) {
            return orderStatus.get();
        }
        throw new IllegalArgumentException(String.format("value '%s' is not a valid OrderStatus value", value));
    }

    public static Optional<OrderStatus> fromValueOptional(Integer value) {
        return Optional.ofNullable(VALUE_MAP.get(value));
    }

}
