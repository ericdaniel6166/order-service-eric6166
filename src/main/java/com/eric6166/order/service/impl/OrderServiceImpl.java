package com.eric6166.order.service.impl;

import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppException;
import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.base.exception.AppValidationException;
import com.eric6166.base.utils.BaseUtils;
import com.eric6166.base.utils.DateTimeUtils;
import com.eric6166.common.config.kafka.KafkaUtils;
import com.eric6166.common.config.kafka.AppEvent;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.order.config.kafka.KafkaProducerProps;
import com.eric6166.order.dto.OrderCreatedEventPayload;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.OrderResponse;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;
import com.eric6166.order.repository.OrderRepository;
import com.eric6166.order.service.OrderService;
import com.eric6166.security.utils.AppSecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
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
public class OrderServiceImpl implements OrderService {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaProducerProps kafkaProducerProps;
    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;
    private final ModelMapper modelMapper;

    @Override
    public MessageResponse placeOrderMqtt(OrderRequest request) {
        return null;
    }

    @Transactional
    @Override
    public MessageResponse placeOrderKafka(OrderRequest request) throws JsonProcessingException, AppException {
        var savedOrder = orderRepository.saveAndFlush(Order.builder()
                .orderId(Order.OrderId.builder()
                        .orderDate(LocalDateTime.now(DateTimeUtils.DEFAULT_ZONE_ID).truncatedTo(ChronoUnit.MICROS))
                        .username(AppSecurityUtils.getUsername())
                        .orderStatusValue(OrderStatus.ORDER_CREATED.getValue())
                        .build())
                .orderDetail(objectMapper.writeValueAsString(request))
                .build());
        var orderCreatedEvent = AppEvent.builder()
                .payload(OrderCreatedEventPayload.builder()
                        .orderDate(DateTimeUtils.toString(savedOrder.getOrderId().getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER))
                        .username(savedOrder.getOrderId().getUsername())
                        .itemList(request.getItemList().stream()
                                .map(item -> modelMapper.map(item, OrderCreatedEventPayload.Item.class))
                                .toList())
                        .build())
                .uuid(UUID.randomUUID().toString())
                .build();
        var sendResult = kafkaTemplate.send(kafkaProducerProps.getOrderCreatedTopicName(), orderCreatedEvent);
        KafkaUtils.handleSendResult(orderCreatedEvent, sendResult);
        return MessageResponse.builder()
                .uuid(BaseUtils.encode(DateTimeUtils.toString(savedOrder.getOrderId().getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER)))
                .message("Order Successfully Created")
                .build();
    }


    @Transactional
    @Override
    public void handleOrderEvent(String username, Object payload, String orderDate, OrderStatus orderStatus,
                                 BigDecimal totalAmount) throws JsonProcessingException {
        orderRepository.saveAndFlush(Order.builder()
                .orderId(Order.OrderId.builder()
                        .orderDate(DateTimeUtils.toLocalDateTime(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER))
                        .username(username)
                        .orderStatusValue(orderStatus.getValue())
                        .build())
                .orderDetail(objectMapper.writeValueAsString(payload))
                .totalAmount(totalAmount)
                .build());
    }

    @Override
    public OrderResponse getOrderByUuidAndUsername(String uuid, String username) throws AppException, JsonProcessingException {
        var orderDate = getOrderDate(uuid);
        var order = orderRepository.getOrderByOrderDateAndUsername(orderDate, username).orElseThrow(()
                -> new AppNotFoundException(String.format("order with uuid '%s'", uuid),
                String.format("order with uuid '%s', orderDate '%s', username '%s' not found", uuid, orderDate, username)));
        var orderResponse = new OrderResponse();
        orderResponse.setOrderDate(order.getOrderId().getOrderDate());
        orderResponse.setOrderStatus(OrderStatus.fromValue(order.getOrderId().getOrderStatusValue()).name());
        orderResponse.setTotalAmount(order.getTotalAmount());
        var orderDetail = objectMapper.readTree(order.getOrderDetail());
        ((ObjectNode) orderDetail).remove(List.of("orderDate", "username", "totalAmount"));
        orderResponse.setOrderDetail(orderDetail);
        return orderResponse;
    }

    private LocalDateTime getOrderDate(String uuid) throws AppException {
        var orderDateText = BaseUtils.decodeOptional(uuid)
                .orElseThrow(() -> new AppValidationException(String.format("order with uuid '%s' is not valid", uuid), String.format("order with uuid '%s', uuid is not in valid Base64 scheme", uuid)));
        return DateTimeUtils.toOptionalLocalDateTime(orderDateText, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER)
                .orElseThrow(() -> new AppValidationException(String.format("order with uuid '%s' is not valid", uuid), String.format("order with uuid '%s', orderDateText '%s' cannot be parsed", uuid, orderDateText)));
    }

    @Override
    public List<OrderResponse> getOrderHistoryByUuidAndUsername(String uuid, String username) throws AppException {
        var orderDate = getOrderDate(uuid);
        var orderList = orderRepository.getOrderHistoryByOrderDateAndUsername(orderDate, username);
        if (orderList.isEmpty()) {
            throw new AppNotFoundException(String.format("order with uuid '%s'", uuid),
                    String.format("order with uuid '%s', orderDate '%s', username '%s'", uuid, orderDate, username));
        }
        return orderList.stream().map(order -> OrderResponse.builder()
                        .orderDate(order.getOrderId().getOrderDate())
                        .orderStatus(OrderStatus.fromValue(order.getOrderId().getOrderStatusValue()).name())
                        .totalAmount(order.getTotalAmount())
                        .build())
                .toList();
    }

    private List<OrderResponse> buildOrderDtoList(Stream<OrderDto> orders) {
        return orders.map(order -> OrderResponse.builder()
                        .orderDate(order.getOrderDate())
                        .orderStatus(OrderStatus.fromValue(order.getOrderStatusValue()).name())
                        .totalAmount(order.getTotalAmount())
                        .uuid(BaseUtils.encode(DateTimeUtils.toString(order.getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER)))
                        .build())
                .toList();
    }

    @Override
    public PageResponse<OrderResponse> getOrderHistoryByUsername(String username, int days, Integer pageNumber, Integer pageSize) {
        var page = orderRepository.findAllOrderByUsername(username, days, pageNumber, pageSize);
        return new PageResponse<>(page.hasContent() ? buildOrderDtoList(page.stream()) : new ArrayList<>(), page);
    }

}
