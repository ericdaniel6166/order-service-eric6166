package com.eric6166.order.service;

import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.enums.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    MessageResponse placeOrderKafka(OrderRequest request) throws JsonProcessingException;

    void handleOrderEvent(String orderUuid, String username, Object payload, String orderDate, OrderStatus orderStatus, BigDecimal totalAmount) throws JsonProcessingException;

    OrderDto getOrderByUuidAndUsername(String uuid, String username) throws AppNotFoundException, JsonProcessingException;

    List<OrderDto> getOrderHistoryByUuidAndUsername(String uuid, String username) throws AppNotFoundException, JsonProcessingException;

    PageResponse<OrderDto> getOrderHistoryByUsername(String username, Integer pageNumber, Integer pageSize) throws JsonProcessingException;
}
