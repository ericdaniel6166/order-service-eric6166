package com.eric6166.order.controller;

import com.eric6166.base.dto.AppResponse;
import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.utils.BaseConst;
import com.eric6166.base.utils.BaseUtils;
import com.eric6166.base.utils.DateTimeUtils;
import com.eric6166.base.utils.TestConst;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.jpa.utils.PageUtils;
import com.eric6166.order.dto.InventoryReservedEventPayload;
import com.eric6166.order.dto.InventoryReservedFailedEventPayload;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.OrderResponse;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;
import com.eric6166.order.service.OrderService;
import com.eric6166.order.utils.TestUtils;
import com.eric6166.security.utils.AppSecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

@WebMvcTest(controllers = {OrderController.class})
class OrderControllerTest {

    private static final String URL_TEMPLATE = "/order";
    private static OrderRequest.Item item;
    private static OrderRequest.Item item1;
    private static OrderRequest orderRequest;
    private static Order order;
    private static Order order1;
    private static OrderResponse orderResponse;
    private static OrderResponse orderResponse1;
    private static String username;
    private static MockedStatic<AppSecurityUtils> appSecurityUtilsMockedStatic;

    @Autowired
    private MockMvc mvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockBean
    private OrderService orderService;

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
        order.setOrderDetail(objectMapper.writeValueAsString(orderDetail));
        orderResponse = TestUtils.mockOrderResponse(order);

        order1 = TestUtils.mockOrder(username, OrderStatus.INVENTORY_RESERVED, null, null);
        var inventoryReservedItem = TestUtils.mockInventoryReservedItem(item, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var inventoryReservedItem1 = TestUtils.mockInventoryReservedItem(item1, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var orderDetail1 = TestUtils.mockInventoryReservedEventPayload(username, inventoryReservedItem, inventoryReservedItem1);
        order1.setOrderDetail(objectMapper.writeValueAsString(orderDetail1));
        orderResponse1 = TestUtils.mockOrderResponse(order1);
    }

    @Test
    void placeOrderKafka_thenReturnOk() throws Exception {
        var messageResponse = MessageResponse.builder()
                .uuid(BaseUtils.encode(DateTimeUtils.toString(order.getOrderId().getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_TIME_FORMATTER)))
                .message("Order Successfully Placed")
                .build();
        Mockito.when(orderService.placeOrderKafka(orderRequest)).thenReturn(messageResponse);
        var expected = new AppResponse<>(messageResponse);
        mvc.perform(MockMvcRequestBuilders
                        .post(URL_TEMPLATE + "/place-order-kafka")
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderRequest))
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    void getOrderStatusByUuid_thenReturnOk() throws Exception {
        var order2 = TestUtils.mockOrder(username, OrderStatus.INVENTORY_RESERVED_FAILED, null, null);
        var inventoryReservedItem = TestUtils.mockInventoryReservedItem(item, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var inventoryReservedItem1 = TestUtils.mockInventoryReservedItem(item1, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var orderDetail2 = InventoryReservedEventPayload.builder()
                .username(username)
                .itemList(List.of(inventoryReservedItem, inventoryReservedItem1))
                .build();
        order2.setOrderDetail(objectMapper.writeValueAsString(orderDetail2));
        var orderResponse2 = TestUtils.mockOrderResponse(order2);
        var orderDetail1 = TestUtils.mockInventoryReservedEventPayload(username, inventoryReservedItem, inventoryReservedItem1);
        orderResponse2.setOrderDetail(orderDetail1);
        var uuid = BaseUtils.encode(DateTimeUtils.toString(order2.getOrderId().getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_FORMATTER));
        var expected = new AppResponse<>(orderResponse2);

        Mockito.when(orderService.getOrderByUuidAndUsername(uuid, username)).thenReturn(orderResponse2);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_TEMPLATE + "/" + username + "/uuid/" + uuid)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    void getOrderHistoryByUuid_thenReturnOk() throws Exception {
        var uuid = BaseUtils.encode(DateTimeUtils.toString(order.getOrderId().getOrderDate(), DateTimeUtils.DEFAULT_LOCAL_DATE_FORMATTER));

        var orderDtoList = List.of(orderResponse, orderResponse1);
        var expected = new AppResponse<>(orderDtoList);

        Mockito.when(orderService.getOrderHistoryByUuidAndUsername(uuid, username)).thenReturn(orderDtoList);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_TEMPLATE + "/" + username + "/history/uuid/" + uuid)
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expected)));
    }

    @Test
    void getOrderHistoryByUsername_thenReturnOk() throws Exception {
        var order2 = TestUtils.mockOrder(username, OrderStatus.INVENTORY_RESERVED_FAILED, null, null);
        var inventoryReservedFailedItem = TestUtils.mockInventoryReservedFailedItem(item, RandomUtils.nextInt(0, item.getOrderQuantity() - 1));
        var inventoryReservedFailedItem1 = TestUtils.mockInventoryReservedFailedItem(item1, null);
        var orderDetail2 = InventoryReservedFailedEventPayload.builder()
                .username(username)
                .itemList(List.of(inventoryReservedFailedItem, inventoryReservedFailedItem1))
                .build();
        order2.setOrderDetail(objectMapper.writeValueAsString(orderDetail2));
        var orderResponse2 = TestUtils.mockOrderResponse(order2);
        var pageNumber = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_NUMBER, BaseConst.DEFAULT_MAX_INTEGER);
        var pageSize = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_SIZE, BaseConst.MAXIMUM_PAGE_SIZE);
        var pageable = PageUtils.buildSimplePageable(pageNumber, pageSize);
        var orderResponseList = List.of(orderResponse2, orderResponse1);
        var pageResponse = new PageResponse<>(orderResponseList, new PageImpl<>(orderResponseList, pageable, orderResponseList.size()));
        var expected = new AppResponse<>(pageResponse);

        int days = 30;
        Mockito.when(orderService.getOrderHistoryByUsername(username, days, pageNumber, pageSize)).thenReturn(pageResponse);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_TEMPLATE + "/" + username + "/history")
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("days", String.valueOf(days))
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expected)));

    }

    @Test
    void getOrderHistoryByUsername_givenDataHasNoContent_thenReturnNoContent() throws Exception {
        var pageNumber = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_NUMBER, BaseConst.DEFAULT_MAX_INTEGER);
        var pageSize = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_SIZE, BaseConst.MAXIMUM_PAGE_SIZE);
        var pageable = PageUtils.buildSimplePageable(pageNumber, pageSize);
        List<OrderResponse> orderResponseList = new ArrayList<>();
        var pageResponse = new PageResponse<>(orderResponseList, new PageImpl<>(orderResponseList, pageable, orderResponseList.size()));

        int days = 30;
        Mockito.when(orderService.getOrderHistoryByUsername(username, days, pageNumber, pageSize)).thenReturn(pageResponse);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_TEMPLATE + "/" + username + "/history")
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("days", String.valueOf(days))
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void getOrderHistoryByUsername_givenPageNumberLessThan1_thenThrowConstraintViolationException() {
        var servletException = Assertions.assertThrows(ServletException.class,
                () -> {
                    var pageNumber = -RandomUtils.nextInt(0, BaseConst.DEFAULT_MAX_INTEGER);
                    var pageSize = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_SIZE, BaseConst.MAXIMUM_PAGE_SIZE);

                    mvc.perform(MockMvcRequestBuilders
                            .get(URL_TEMPLATE + "/" + username + "/history")
                            .with(SecurityMockMvcRequestPostProcessors
                                    .jwt()
                                    .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON_VALUE)
                            .characterEncoding(StandardCharsets.UTF_8)
                            .param("pageNumber", String.valueOf(pageNumber))
                            .param("pageSize", String.valueOf(pageSize)));
                });

        var expected = "getOrderHistoryByUsername.pageNumber: must be greater than or equal to 1";

        Assertions.assertTrue(servletException.getCause() instanceof ConstraintViolationException);
        Assertions.assertEquals(expected, servletException.getCause().getMessage());
    }
}