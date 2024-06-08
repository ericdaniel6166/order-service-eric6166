package com.eric6166.order.repository;

import com.eric6166.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findFirstByUuidOrderByOrderStatusValueDesc(String uuid);

    List<Order> findByUuidOrderByOrderStatusValueDesc(String uuid);

    @Query(value = """
            SELECT O.*
            FROM T_ORDER O
                     JOIN (
                SELECT O1.ORDER_DATE, MAX(O1.ORDER_STATUS_VALUE) AS MAX_STATUS
                FROM T_ORDER O1
                WHERE O1.USERNAME = :username
                GROUP BY O1.ORDER_DATE
            ) O2
                          ON O.USERNAME = :username
                                 AND O.ORDER_DATE = O2.ORDER_DATE
                                 AND O.ORDER_STATUS_VALUE = O2.MAX_STATUS
            """, nativeQuery = true)
    Page<Order> findAllOrderByUsername(String username, Pageable pageable);
}
