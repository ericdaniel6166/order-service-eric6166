package com.eric6166.order.service;

import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.enums.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;

public interface OrderService {

    Object placeOrderKafka(OrderRequest request) throws JsonProcessingException;

    void handleOrderEvent(String orderUuid, String username, Object payload, OrderStatus itemNotAvailable, BigDecimal totalAmount) throws JsonProcessingException;

    Object getOrderStatusByUuid(String uuid) throws AppNotFoundException, JsonProcessingException;

    Object getOrderHistoryByUuid(String uuid) throws AppNotFoundException, JsonProcessingException;

    PageResponse<OrderDto> getOrderHistoryByUsername(String username, Integer pageNumber, Integer pageSize) throws JsonProcessingException, AppNotFoundException;
}
