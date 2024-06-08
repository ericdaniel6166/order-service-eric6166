package com.eric6166.order.dto;

import com.eric6166.jpa.model.BaseEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@EqualsAndHashCode(callSuper = false)
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@ToString(callSuper = true)
public class OrderDto extends BaseEntity<String> implements Serializable {
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
