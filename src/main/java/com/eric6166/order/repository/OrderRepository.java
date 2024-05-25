package com.eric6166.order.repository;

import com.eric6166.order.model.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Optional<Order> findFirstByUuidOrderByIdDesc(String uuid);

    List<Order> findByUuidOrderByIdDesc(String uuid);

    @Query("""
            select o from Order o 
            where o.username = :username and o.id in 
            (select max(o1.id) from Order o1 where o1.username = :username group by o1.uuid)
            """)
    Page<Order> findAllOrderByUsername(String username, Pageable pageable);
}
