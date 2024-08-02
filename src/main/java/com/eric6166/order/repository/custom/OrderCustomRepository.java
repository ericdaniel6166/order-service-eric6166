package com.eric6166.order.repository.custom;

import com.eric6166.order.dto.OrderDto;
import org.springframework.data.domain.Page;

public interface OrderCustomRepository {

    Page<OrderDto> findAllOrderByUsername(String username, Integer pageNumber, Integer pageSize);

}
