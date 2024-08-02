package com.eric6166.order.model;

import com.eric6166.jpa.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table(name = "T_ORDER")
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Order extends BaseEntity<String> {

    @EmbeddedId
    private OrderId orderId;

    @Column(name = "ORDER_DETAIL", columnDefinition = "TEXT")
    private String orderDetail;

    @Column(name = "TOTAL_AMOUNT", precision = 19, scale = 4)
    private BigDecimal totalAmount;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Embeddable
    public static class OrderId implements Serializable {

        @Serial
        private static final long serialVersionUID = 1L;

        @Column(name = "ORDER_DATE", nullable = false)
        private LocalDateTime orderDate;

        @Column(name = "USERNAME", nullable = false)
        private String username;

        @Column(name = "ORDER_STATUS_VALUE", nullable = false)
        private Float orderStatusValue;

    }

}
