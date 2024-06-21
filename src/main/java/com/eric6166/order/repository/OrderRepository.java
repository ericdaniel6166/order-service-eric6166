package com.eric6166.order.repository;

import com.eric6166.order.model.Order;
import com.eric6166.order.repository.custom.OrderCustomRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long>, OrderCustomRepository {

    Optional<Order> findFirstByUuidAndUsernameOrderByOrderStatusValueDesc(String uuid, String username);

    List<Order> findByUuidAndUsernameOrderByOrderStatusValueDesc(String uuid, String username);

}
