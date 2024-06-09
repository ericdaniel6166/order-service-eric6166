package com.eric6166.order.repository;

import com.eric6166.order.model.Order;
import com.eric6166.order.repository.custom.OrderCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderCustomRepository {

    Optional<Order> findFirstByUuidOrderByOrderStatusValueDesc(String uuid);

    List<Order> findByUuidOrderByOrderStatusValueDesc(String uuid);

}
