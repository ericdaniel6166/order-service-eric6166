package com.eric6166.order.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    private LocalDateTime orderDate;
    private String username;
    private String uuid;
    private String orderStatus;
    private BigDecimal totalAmount;
    private Object orderDetail;
}
