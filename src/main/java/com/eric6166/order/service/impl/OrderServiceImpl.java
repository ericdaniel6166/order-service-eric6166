package com.eric6166.order.service.impl;

import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.base.utils.DateTimeUtils;
import com.eric6166.common.config.kafka.AppEvent;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.order.config.kafka.KafkaProducerProps;
import com.eric6166.order.dto.OrderCreatedEventPayload;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
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
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

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
    public MessageResponse placeOrderKafka(OrderRequest request) throws JsonProcessingException {
        var orderDate = LocalDateTime.now().truncatedTo(ChronoUnit.MICROS);
        var order = Order.builder()
                .uuid(UUID.randomUUID().toString())
                .username(AppSecurityUtils.getUsername())
                .orderDetail(objectMapper.writeValueAsString(request))
                .orderStatusValue(OrderStatus.ORDER_CREATED.getValue())
                .orderDate(orderDate)
                .build();
        var savedOrder = orderRepository.saveAndFlush(order);
        var orderCreatedEvent = AppEvent.builder()
                .payload(OrderCreatedEventPayload.builder()
                        .orderDate(DateTimeUtils.toString(savedOrder.getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER))
                        .orderUuid(savedOrder.getUuid())
                        .username(savedOrder.getUsername())
                        .itemList(request.getItemList().stream()
                                .map(item -> modelMapper.map(item, OrderCreatedEventPayload.Item.class))
                                .toList())
                        .build())
                .uuid(UUID.randomUUID().toString())
                .build();
        kafkaTemplate.send(kafkaProducerProps.getOrderCreatedTopicName(), orderCreatedEvent);
        return MessageResponse.builder()
                .uuid(savedOrder.getUuid())
                .message("Order Successfully Created")
                .build();
    }

    @Transactional
    @Override
    public void handleOrderEvent(String uuid, String username, Object payload, String orderDate, OrderStatus orderStatus,
                                 BigDecimal totalAmount) throws JsonProcessingException {
        var order = Order.builder()
                .uuid(uuid)
                .username(username)
                .orderDetail(objectMapper.writeValueAsString(payload))
                .orderDate(DateTimeUtils.toLocalDateTime(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER))
                .orderStatusValue(orderStatus.getValue())
                .totalAmount(totalAmount)
                .build();
        orderRepository.saveAndFlush(order);
    }

    @Override
    public OrderDto getOrderByUuidAndUsername(String uuid, String username) throws AppNotFoundException, JsonProcessingException {
        var order = orderRepository.findFirstByUuidAndUsernameOrderByOrderStatusValueDesc(uuid, username).orElseThrow(()
                -> new AppNotFoundException(String.format("order with uuid '%s'", uuid)));
        var orderDto = modelMapper.map(order, OrderDto.class);
        orderDto.setOrderDetail(objectMapper.readTree(order.getOrderDetail()));
        orderDto.setOrderStatus(OrderStatus.fromValue(order.getOrderStatusValue()).name());
        return orderDto;
    }

    @Override
    public List<OrderDto> getOrderHistoryByUuidAndUsername(String uuid, String username) throws AppNotFoundException, JsonProcessingException {
        var orderList = orderRepository.findByUuidAndUsernameOrderByOrderStatusValueDesc(uuid, username);
        if (orderList.isEmpty()) {
            throw new AppNotFoundException(String.format("order with uuid '%s'", uuid));
        }
        return buildOrderDtoList(orderList.stream());
    }

    private List<OrderDto> buildOrderDtoList(Stream<Order> orders) throws JsonProcessingException {
        return orders.map(order -> {
            var orderDto = modelMapper.map(order, OrderDto.class);
//            orderDto.setOrderDetail(objectMapper.readTree(order.getOrderDetail()));
            orderDto.setOrderDetail(null);
            orderDto.setOrderStatus(OrderStatus.fromValue(order.getOrderStatusValue()).name());
//            orderDto.setOrderStatus(OrderStatus.fromValue(100).name());
            return orderDto;
        }).toList();
    }

    @Override
    public PageResponse<OrderDto> getOrderHistoryByUsername(String username, Integer pageNumber, Integer pageSize)
            throws JsonProcessingException {
        var page = orderRepository.findAllOrderByUsername(username, pageNumber, pageSize);
        if (page.isEmpty()) {
            return new PageResponse<>(new ArrayList<>(), page);
        }
        var orderDtoList = buildOrderDtoList(page.stream());
        return new PageResponse<>(orderDtoList, page);
    }

}
