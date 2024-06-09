package com.eric6166.order.repository.custom;

import com.eric6166.order.model.Order;
import org.springframework.data.domain.Page;

public interface OrderCustomRepository {

    Page<Order> findAllOrderByUsername(String username, Integer pageNumber, Integer pageSize);

}
