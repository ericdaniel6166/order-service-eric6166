package com.eric6166.order.repository;

import com.eric6166.order.model.Order;
import com.eric6166.order.repository.custom.OrderCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Order.OrderId>, OrderCustomRepository {

    @Query(value = """
            SELECT *
            FROM T_ORDER
            WHERE ORDER_DATE = :orderDate
              AND USERNAME = :username
            ORDER BY ORDER_STATUS_VALUE DESC
            """, nativeQuery = true)
    List<Order> getOrderHistoryByOrderDateAndUsername(LocalDateTime orderDate, String username);

    @Query(value = """
            SELECT *
            FROM T_ORDER
            WHERE ORDER_DATE = :orderDate
              AND USERNAME = :username
            ORDER BY ORDER_STATUS_VALUE DESC
            LIMIT 1
            """, nativeQuery = true)
    Optional<Order> getOrderByOrderDateAndUsername(LocalDateTime orderDate, String username);

}
