package com.eric6166.order.repository;

import com.eric6166.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findFirstByUuidOrderByIdDesc(String uuid);

    List<Order> findByUuidOrderByIdDesc(String uuid);
}
