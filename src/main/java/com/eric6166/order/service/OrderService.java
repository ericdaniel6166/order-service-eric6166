package com.eric6166.order.service;

import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppException;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.OrderResponse;
import com.eric6166.order.enums.OrderStatus;
import com.fasterxml.jackson.core.JsonProcessingException;

import java.math.BigDecimal;
import java.util.List;

public interface OrderService {

    MessageResponse placeOrderKafka(OrderRequest request) throws JsonProcessingException, AppException;

    void handleOrderEvent(String username, Object payload, String orderDate, OrderStatus orderStatus, BigDecimal totalAmount) throws JsonProcessingException;

    OrderResponse getOrderByUuidAndUsername(String uuid, String username) throws AppException, JsonProcessingException;

    List<OrderResponse> getOrderHistoryByUuidAndUsername(String uuid, String username) throws AppException;

    PageResponse<OrderResponse> getOrderHistoryByUsername(String username, int days, Integer pageNumber, Integer pageSize);

    MessageResponse placeOrderMqtt(OrderRequest request);
}
