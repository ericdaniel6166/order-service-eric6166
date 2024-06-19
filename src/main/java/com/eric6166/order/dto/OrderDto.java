package com.eric6166.order.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    Long id;
    String uuid;
    String username;
    String orderStatus;
    LocalDateTime orderDate;
    BigDecimal totalAmount;
    Object orderDetail;
}
