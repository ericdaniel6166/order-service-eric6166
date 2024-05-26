package com.eric6166.order.service.impl;

import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.base.utils.BaseConst;
import com.eric6166.common.config.kafka.AppEvent;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.jpa.utils.PageUtils;
import com.eric6166.order.config.kafka.KafkaProducerProps;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.PlaceOrderEventPayload;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;
import com.eric6166.order.repository.OrderRepository;
import com.eric6166.order.service.OrderService;
import com.eric6166.security.utils.AppSecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class OrderServiceImpl implements OrderService {

    KafkaTemplate<String, Object> kafkaTemplate;
    KafkaProducerProps kafkaProducerProps;
    OrderRepository orderRepository;
    ObjectMapper objectMapper;
    ModelMapper modelMapper;

    @Transactional
    @Override
    public Object placeOrderKafka(OrderRequest request) throws JsonProcessingException {
        var order = Order.builder()
                .uuid(UUID.randomUUID().toString())
                .username(AppSecurityUtils.getUsername())
                .orderDetail(objectMapper.writeValueAsString(request))
                .status(OrderStatus.PLACE_ORDER.name())
                .build();
        var savedOrder = orderRepository.saveAndFlush(order);
        var placeOrderEvent = AppEvent.builder()
                .payload(PlaceOrderEventPayload.builder()
                        .orderUuid(savedOrder.getUuid())
                        .username(savedOrder.getUsername())
                        .itemList(request.getItemList().stream()
                                .map(item -> modelMapper.map(item, PlaceOrderEventPayload.Item.class))
                                .toList())
                        .build())
                .uuid(UUID.randomUUID().toString())
                .build();
        kafkaTemplate.send(kafkaProducerProps.getPlaceOrderTopicName(), placeOrderEvent);
        return MessageResponse.builder()
                .uuid(savedOrder.getUuid())
                .message("Order Successfully Placed")
                .build();
    }

    @Transactional
    @Override
    public void handleOrderEvent(String uuid, String username, Object payload, OrderStatus orderStatus, BigDecimal totalAmount) throws JsonProcessingException {
        var order = Order.builder()
                .uuid(uuid)
                .username(username)
                .orderDetail(objectMapper.writeValueAsString(payload))
                .status(orderStatus.name())
                .totalAmount(totalAmount)
                .build();
        orderRepository.saveAndFlush(order);
    }

    @Override
    public Object getOrderStatusByUuid(String uuid) throws AppNotFoundException, JsonProcessingException {
        var order = orderRepository.findFirstByUuidOrderByIdDesc(uuid).orElseThrow(()
                -> new AppNotFoundException(String.format("order with uuid '%s'", uuid)));
        var orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setOrderDetail(objectMapper.readTree(order.getOrderDetail()));
        return orderDto;
    }

    @Override
    public Object getOrderHistoryByUuid(String uuid) throws AppNotFoundException, JsonProcessingException {
        List<Order> orderList = orderRepository.findByUuidOrderByIdDesc(uuid);
        if (orderList.isEmpty()) {
            throw new AppNotFoundException(String.format("order with uuid '%s'", uuid));
        }
        return buildOrderDtoList(orderList);
    }

    private List<OrderDto> buildOrderDtoList(Iterable<Order> orders) throws JsonProcessingException {
        List<OrderDto> orderDtoList = new ArrayList<>();
        for (var order : orders) {
            var orderDto = modelMapper.map(order, OrderDto.class);
            orderDto.setOrderDetail(objectMapper.readTree(order.getOrderDetail()));
            orderDtoList.add(orderDto);
        }
        return orderDtoList;
    }

    @Override
    public PageResponse<OrderDto> getOrderHistoryByUsername(String username, Integer pageNumber, Integer pageSize) throws JsonProcessingException {
        var pageable = PageUtils.buildPageable(pageNumber, pageSize, BaseConst.ID, Sort.Direction.DESC.name());
        var page = orderRepository.findAllOrderByUsername(username, pageable);
        var orderDtoList = buildOrderDtoList(page);
        return new PageResponse<>(orderDtoList, page);
    }

}
