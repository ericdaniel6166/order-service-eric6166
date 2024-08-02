package com.eric6166.order.service.impl;

import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppException;
import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.base.utils.BaseConst;
import com.eric6166.base.utils.BaseUtils;
import com.eric6166.base.utils.DateTimeUtils;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.jpa.utils.PageUtils;
import com.eric6166.order.config.kafka.KafkaProducerProps;
import com.eric6166.order.dto.OrderCreatedEventPayload;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.OrderResponse;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;
import com.eric6166.order.repository.OrderRepository;
import com.eric6166.order.utils.TestUtils;
import com.eric6166.security.utils.AppSecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageImpl;
import org.springframework.kafka.core.KafkaTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    private static OrderRequest.Item item;
    private static OrderRequest.Item item1;
    private static OrderRequest orderRequest;
    private static Order order;
    private static Order order1;
    private static OrderResponse orderResponse;
    private static OrderResponse orderResponse1;
    private static String username;

    private static MockedStatic<AppSecurityUtils> appSecurityUtilsMockedStatic;

    @InjectMocks
    private OrderServiceImpl orderService;
    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Mock
    private KafkaProducerProps kafkaProducerProps;
    @Mock
    private OrderRepository orderRepository;
    @Mock
    private ObjectMapper objectMapper;
    @Mock
    private ModelMapper modelMapper;

    @BeforeAll
    static void setUpAll() {
        item = TestUtils.mockOrderRequestItem(RandomUtils.nextLong(1, 100), RandomUtils.nextInt(1, 10000));
        item1 = TestUtils.mockOrderRequestItem(RandomUtils.nextLong(101, 200), RandomUtils.nextInt(1, 10000));
        orderRequest = TestUtils.mockOrderRequest(item, item1);
        username = RandomStringUtils.random(30);
        appSecurityUtilsMockedStatic = Mockito.mockStatic(AppSecurityUtils.class);
        appSecurityUtilsMockedStatic.when(AppSecurityUtils::getUsername).thenReturn(username);

    }

    @AfterAll
    static void tearDownAll() {
        appSecurityUtilsMockedStatic.close();

    }

    @BeforeEach
    void setUp() throws JsonProcessingException {
        order = TestUtils.mockOrder(username, OrderStatus.ORDER_CREATED, null, null);
        var orderDetail = TestUtils.mockOrderRequest(item, item1);
        ObjectMapper mapper = new ObjectMapper();
        order.setOrderDetail(mapper.writeValueAsString(orderDetail));
        orderResponse = TestUtils.mockOrderResponse(order);

        order1 = TestUtils.mockOrder(username, OrderStatus.INVENTORY_RESERVED, null, null);
        var inventoryReservedItem = TestUtils.mockInventoryReservedItem(item, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var inventoryReservedItem1 = TestUtils.mockInventoryReservedItem(item1, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var orderDetail1 = TestUtils.mockInventoryReservedEventPayload(username, inventoryReservedItem, inventoryReservedItem1);
        order1.setOrderDetail(mapper.writeValueAsString(orderDetail1));
        orderResponse1 = TestUtils.mockOrderResponse(order1);
    }

    @Test
    void placeOrderKafka_thenReturnSuccess() throws JsonProcessingException {
        var orderDate = LocalDateTime.now();
        var mapper = new ObjectMapper();
        var savedOrder = Order.builder()
                .orderId(Order.OrderId.builder()
                        .username(username)
                        .orderDate(orderDate)
                        .orderStatusValue(OrderStatus.ORDER_CREATED.getValue())
                        .build())
                .orderDetail(mapper.writeValueAsString(orderRequest))
                .build();
        var orderCreatedItem = TestUtils.mockOrderCreatedItem(item);
        var orderCreatedItem1 = TestUtils.mockOrderCreatedItem(item1);
        var expected = MessageResponse.builder()
                .uuid(BaseUtils.encode(DateTimeUtils.toString(savedOrder.getOrderId().getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER)))
                .message("Order Successfully Created")
                .build();

        Mockito.when(orderRepository.saveAndFlush(Mockito.any(Order.class))).thenReturn(savedOrder);
        Mockito.when(modelMapper.map(item, OrderCreatedEventPayload.Item.class)).thenReturn(orderCreatedItem);
        Mockito.when(modelMapper.map(item1, OrderCreatedEventPayload.Item.class)).thenReturn(orderCreatedItem1);

        var actual = orderService.placeOrderKafka(orderRequest);

        Assertions.assertEquals(expected, actual);
    }


    @Test
    void handleOrderEvent_thenReturnSuccess() throws JsonProcessingException {
        var inventoryReservedItem = TestUtils.mockInventoryReservedItem(item, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var inventoryReservedItem1 = TestUtils.mockInventoryReservedItem(item1, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var payload = TestUtils.mockInventoryReservedEventPayload(username, inventoryReservedItem, inventoryReservedItem1);
        var orderDate = LocalDateTime.now();
        var orderDateStr = DateTimeUtils.toString(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER);
        var orderStatus = OrderStatus.INVENTORY_RESERVED;
        BigDecimal totalAmount = null;
        var mapper = new ObjectMapper();
        var orderDetail = mapper.writeValueAsString(payload);
        var order = Order.builder()
                .orderId(Order.OrderId.builder()
                        .username(username)
                        .orderDate(orderDate)
                        .orderStatusValue(orderStatus.getValue())
                        .build())
                .orderDetail(orderDetail)
                .totalAmount(totalAmount)
                .build();
        Mockito.when(objectMapper.writeValueAsString(payload)).thenReturn(orderDetail);
        orderService.handleOrderEvent(username, payload, orderDateStr, orderStatus, totalAmount);

        Mockito.verify(orderRepository, Mockito.times(1)).saveAndFlush(order);
    }

    @Test
    void getOrderByUuidAndUsername_thenReturnSuccess() throws AppException, JsonProcessingException {
        var mapper = new ObjectMapper();
        var order2 = TestUtils.mockOrder(username, OrderStatus.ORDER_CREATED, mapper.writeValueAsString(orderRequest), null);
        var orderDate = order2.getOrderId().getOrderDate();
        var uuid = BaseUtils.encode(DateTimeUtils.toString(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER));
        Mockito.when(orderRepository.getOrderByOrderDateAndUsername(orderDate, username)).thenReturn(Optional.of(order2));
        Mockito.when(objectMapper.readTree(order2.getOrderDetail())).thenReturn(mapper.readTree(order2.getOrderDetail()));
        var expected = TestUtils.mockOrderResponse(order2);
        expected.setOrderDetail(order2.getOrderDetail());
        var actual = orderService.getOrderByUuidAndUsername(uuid, username);
        Assertions.assertEquals(expected.toString(), actual.toString());
    }

    @Test
    void getOrderByUuidAndUsername_thenThrowAppNotFoundException() {
        var orderDate = order.getOrderId().getOrderDate();
        var uuid = BaseUtils.encode(DateTimeUtils.toString(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER));

        var e = Assertions.assertThrows(AppNotFoundException.class,
                () -> {
                    Mockito.when(orderRepository.getOrderByOrderDateAndUsername(orderDate, username)).thenReturn(Optional.empty());
                    orderService.getOrderByUuidAndUsername(uuid, username);
                });

        var expected = String.format("order with uuid '%s' not found", uuid);

        Assertions.assertEquals(expected, e.getMessage());
    }

    @Test
    void getOrderHistoryByUuidAndUsername_thenThrowAppNotFoundException() throws AppNotFoundException, JsonProcessingException {
        var orderDate = order.getOrderId().getOrderDate();
        var uuid = BaseUtils.encode(DateTimeUtils.toString(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER));

        var e = Assertions.assertThrows(AppNotFoundException.class,
                () -> {

                    Mockito.when(orderRepository.getOrderHistoryByOrderDateAndUsername(orderDate, username)).thenReturn(new ArrayList<>());
                    orderService.getOrderHistoryByUuidAndUsername(uuid, username);
                });

        var expected = String.format("order with uuid '%s' not found", uuid);

        Assertions.assertEquals(expected, e.getMessage());
    }

    @Test
    void getOrderHistoryByUuidAndUsername_thenReturnSuccess() throws AppException, JsonProcessingException {
        var orderDate = order.getOrderId().getOrderDate();
        var uuid = BaseUtils.encode(DateTimeUtils.toString(orderDate, DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER));


        Mockito.when(orderRepository.getOrderHistoryByOrderDateAndUsername(orderDate, username)).thenReturn(List.of(order1, order));
        var expected = List.of(orderResponse1, orderResponse);
        var actual = orderService.getOrderHistoryByUuidAndUsername(uuid, username);
        Assertions.assertEquals(expected, actual);
    }

    @Test
    void getOrderHistoryByUsername_thenReturnSuccess() {
        var pageNumber = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_NUMBER, BaseConst.DEFAULT_MAX_INTEGER);
        var pageSize = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_SIZE, BaseConst.MAXIMUM_PAGE_SIZE);
        var pageable = PageUtils.buildSimplePageable(pageNumber, pageSize);
        var orderList = List.of(TestUtils.mockOrderDto(order1), TestUtils.mockOrderDto(order));
        var page = new PageImpl<>(orderList, pageable, orderList.size());
        var orderResponse4 = TestUtils.mockOrderResponse(order1);
        orderResponse4.setUuid(BaseUtils.encode(DateTimeUtils.toString(orderResponse4.getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER)));
        var orderResponse3 = TestUtils.mockOrderResponse(order);
        orderResponse3.setUuid(BaseUtils.encode(DateTimeUtils.toString(orderResponse3.getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER)));
        var orderDtoList = List.of(orderResponse4, orderResponse3);
        var expected = new PageResponse<>(orderDtoList, new PageImpl<>(orderList, pageable, orderDtoList.size()));

        Mockito.when(orderRepository.findAllOrderByUsername(username, pageNumber, pageSize)).thenReturn(page);

        var actual = orderService.getOrderHistoryByUsername(username, pageNumber, pageSize);

        Assertions.assertEquals(expected.getContent(), actual.getContent());

    }
}