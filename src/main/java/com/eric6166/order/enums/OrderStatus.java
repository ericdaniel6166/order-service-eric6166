package com.eric6166.order.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Getter
@RequiredArgsConstructor
public enum OrderStatus {
    ORDER_CREATED(0f),
    INVENTORY_RESERVED(1f),
    INVENTORY_RESERVED_FAILED(2f),
    ;

    private static final Map<Float, OrderStatus> VALUE_MAP =
            Arrays.stream(OrderStatus.values())
                    .collect(Collectors.toMap(OrderStatus::getValue, Function.identity()));

    private final Float value;

    public static OrderStatus fromValue(Float value) {
        return fromValueOptional(value).orElseThrow(()
                -> new IllegalArgumentException(String.format("value '%s' is not a valid OrderStatus value", value)));
    }

    public static Optional<OrderStatus> fromValueOptional(Float value) {
        return Optional.ofNullable(VALUE_MAP.get(value));
    }

}
