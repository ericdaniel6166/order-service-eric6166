package com.eric6166.order.model;

import com.eric6166.jpa.model.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Table(name = "T_ORDER")
@Entity
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class Order extends BaseEntity<String> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID")
    private Long id;

    @Column(name = "UUID", nullable = false)
    private String uuid;

    @Column(name = "USERNAME", nullable = false)
    private String username;

    @Column(name = "STATUS", nullable = false)
    private String status;

    @Column(name = "ORDER_DETAIL", columnDefinition = "TEXT")
    private String orderDetail;

    @Column(name = "TOTAL_AMOUNT", precision = 19, scale = 4)
    private BigDecimal totalAmount;

}
