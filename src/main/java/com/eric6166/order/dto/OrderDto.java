package com.eric6166.order.dto;

import jakarta.persistence.Column;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    @Column(name = "ORDER_DATE")
    private LocalDateTime orderDate;
    @Column(name = "USERNAME")
    private String username;
    @Column(name = "ORDER_STATUS_VALUE")
    private Float orderStatusValue;
    @Column(name = "TOTAL_AMOUNT")
    private BigDecimal totalAmount;
    @Column(name = "ORDER_DETAIL")
    private String orderDetail;
}
