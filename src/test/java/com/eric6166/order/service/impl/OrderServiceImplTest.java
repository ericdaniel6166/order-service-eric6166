package com.eric6166.order.service.impl;

import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.base.utils.BaseConst;
import com.eric6166.base.utils.DateTimeUtils;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.jpa.utils.PageUtils;
import com.eric6166.order.config.kafka.KafkaProducerProps;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.PlaceOrderEventPayload;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;
import com.eric6166.order.repository.OrderRepository;
import com.eric6166.order.utils.TestUtils;
import com.eric6166.security.utils.AppSecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Sort;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
@FieldDefaults(level = AccessLevel.PRIVATE)
class OrderServiceImplTest {

    private static OrderRequest.Item item;
    private static OrderRequest.Item item1;
    private static OrderRequest orderRequest;
    private static Order order;
    private static Order order1;
    private static OrderDto orderDto;
    private static OrderDto orderDto1;

    @InjectMocks
    OrderServiceImpl orderService;
    @Mock
    KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    KafkaProducerProps kafkaProducerProps;
    @Mock
    OrderRepository orderRepository;
    @Mock
    ObjectMapper objectMapper;
    @Mock
    ModelMapper modelMapper;

    @BeforeAll
    static void setUpAll() {
        item = TestUtils.mockOrderRequestItem(1L, 100);
        item1 = TestUtils.mockOrderRequestItem(2L, 200);
        orderRequest = TestUtils.mockOrderRequest(item, item1);

    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        var uuid = UUID.randomUUID().toString();
        var username = "customer";

        order = TestUtils.mockOrder(RandomUtils.nextLong(), uuid, username, OrderStatus.PLACE_ORDER, null, null);
        var orderDetail = TestUtils.mockOrderRequest(item, item1);
        order.setOrderDetail(objectMapper.writeValueAsString(orderDetail));
        orderDto = TestUtils.mockOrderDto(order, orderDetail);

        order1 = TestUtils.mockOrder(RandomUtils.nextLong(), uuid, username, OrderStatus.INVENTORY_CHECKED, null, null);
        var inventoryCheckedItem = TestUtils.mockInventoryCheckedItem(item, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var inventoryCheckedItem1 = TestUtils.mockInventoryCheckedItem(item1, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var orderDetail1 = TestUtils.mockInventoryCheckedEventPayload(uuid, username, inventoryCheckedItem, inventoryCheckedItem1);
        order1.setOrderDetail(objectMapper.writeValueAsString(orderDetail1));
        orderDto1 = TestUtils.mockOrderDto(order1, orderDetail1);
    }

    @Test
    void placeOrderKafka_thenReturnSuccess() throws JsonProcessingException {
        var username = "customer";
        var orderDate = LocalDateTime.now();
        var savedOrder = Order.builder()
                .id(RandomUtils.nextLong(1, 100))
                .uuid(UUID.randomUUID().toString())
                .username(username)
                .orderDetail(objectMapper.writeValueAsString(orderRequest))
                .orderStatusValue(OrderStatus.PLACE_ORDER.getOrderStatusValue())
                .orderDate(orderDate)
                .build();
        var placeOrderItem = TestUtils.mockPlaceOrderItem(item);
        var placeOrderItem1 = TestUtils.mockPlaceOrderItem(item1);
        var expected = MessageResponse.builder()
                .uuid(savedOrder.getUuid())
                .message("Order Successfully Placed")
                .build();

        Mockito.when(orderRepository.saveAndFlush(Mockito.any(Order.class))).thenReturn(savedOrder);
        Mockito.when(modelMapper.map(item, PlaceOrderEventPayload.Item.class)).thenReturn(placeOrderItem);
        Mockito.when(modelMapper.map(item1, PlaceOrderEventPayload.Item.class)).thenReturn(placeOrderItem1);
        Mockito.mockStatic(AppSecurityUtils.class).when(AppSecurityUtils::getUsername).thenReturn(username);

        var actual = orderService.placeOrderKafka(orderRequest);

        Assertions.assertEquals(expected, actual);
    }


    @Test
    void handleOrderEvent_thenReturnSuccess() throws JsonProcessingException {
        var username = "customer";
        var uuid = UUID.randomUUID().toString();
        var inventoryCheckedItem = TestUtils.mockInventoryCheckedItem(item, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var inventoryCheckedItem1 = TestUtils.mockInventoryCheckedItem(item1, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var payload = TestUtils.mockInventoryCheckedEventPayload(uuid, username, inventoryCheckedItem, inventoryCheckedItem1);
        var orderDate = LocalDateTime.now();
        var orderDateStr = DateTimeUtils.toString(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER);
        var orderStatus = OrderStatus.INVENTORY_CHECKED;
        BigDecimal totalAmount = null;
        var order = Order.builder()
                .uuid(uuid)
                .username(username)
                .orderDetail(objectMapper.writeValueAsString(payload))
                .orderStatusValue(orderStatus.getOrderStatusValue())
                .orderDate(orderDate)
                .totalAmount(totalAmount)
                .build();

        orderService.handleOrderEvent(uuid, username, payload, orderDateStr, orderStatus, totalAmount);

        Mockito.verify(orderRepository, Mockito.times(1)).saveAndFlush(order);
    }

    @Test
    void getOrderStatusByUuid_thenReturnSuccess() throws AppNotFoundException, JsonProcessingException {
        var uuid = UUID.randomUUID().toString();
        Mockito.when(orderRepository.findFirstByUuidOrderByOrderStatusValueDesc(uuid)).thenReturn(Optional.of(order1));
        Mockito.when(modelMapper.map(order1, OrderDto.class)).thenReturn(orderDto1);
        var expected = orderDto1;
        var actual = orderService.getOrderStatusByUuid(uuid);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getOrderStatusByUuid_thenThrowAppNotFoundException() {
        var uuid = UUID.randomUUID().toString();
        var e = Assertions.assertThrows(AppNotFoundException.class,
                () -> {
                    Mockito.when(orderRepository.findFirstByUuidOrderByOrderStatusValueDesc(uuid)).thenReturn(Optional.empty());
                    orderService.getOrderStatusByUuid(uuid);
                });

        var expected = String.format("order with uuid '%s' not found", uuid);

        Assertions.assertEquals(expected, e.getMessage());
    }

    @Test
    void getOrderHistoryByUuid_thenThrowAppNotFoundException() throws AppNotFoundException, JsonProcessingException {
        var uuid = UUID.randomUUID().toString();
        var e = Assertions.assertThrows(AppNotFoundException.class,
                () -> {
                    Mockito.when(orderRepository.findByUuidOrderByOrderStatusValueDesc(uuid)).thenReturn(new ArrayList<>());
                    orderService.getOrderHistoryByUuid(uuid);
                });

        var expected = String.format("order with uuid '%s' not found", uuid);

        Assertions.assertEquals(expected, e.getMessage());
    }

    @Test
    void getOrderHistoryByUuid_thenReturnSuccess() throws AppNotFoundException, JsonProcessingException {
        var uuid = UUID.randomUUID().toString();
        Mockito.when(orderRepository.findByUuidOrderByOrderStatusValueDesc(uuid)).thenReturn(List.of(order1, order));
        Mockito.when(modelMapper.map(order1, OrderDto.class)).thenReturn(orderDto1);
        Mockito.when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);
        var expected = List.of(orderDto1, orderDto);
        var actual = orderService.getOrderHistoryByUuid(uuid);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getOrderHistoryByUsername_thenReturnSuccess() throws JsonProcessingException {
        var username = "customer";
        var pageNumber = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_NUMBER, BaseConst.DEFAULT_MAX_INTEGER);
        var pageSize = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_SIZE, BaseConst.MAXIMUM_PAGE_SIZE);
        var pageable = PageUtils.buildPageable(pageNumber, pageSize, Order.ORDER_DATE_COLUMN, Sort.Direction.DESC.name());
        var orderList = List.of(order1, order);
        var page = new PageImpl<>(orderList, pageable, orderList.size());
        var orderDtoList = List.of(orderDto1, orderDto);
        var expected = new PageResponse<>(orderDtoList, new PageImpl<>(orderList, pageable, orderDtoList.size()));

        Mockito.when(orderRepository.findAllOrderByUsername(username, pageable)).thenReturn(page);
        Mockito.when(modelMapper.map(order1, OrderDto.class)).thenReturn(orderDto1);
        Mockito.when(modelMapper.map(order, OrderDto.class)).thenReturn(orderDto);

        var actual = orderService.getOrderHistoryByUsername(username, pageNumber, pageSize);

        Assertions.assertEquals(expected.getContent(), actual.getContent());

    }
}