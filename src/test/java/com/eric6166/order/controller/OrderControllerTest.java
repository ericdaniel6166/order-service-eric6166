package com.eric6166.order.controller;

import com.eric6166.base.dto.AppResponse;
import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.utils.BaseConst;
import com.eric6166.base.utils.TestConst;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.jpa.utils.PageUtils;
import com.eric6166.order.dto.InventoryReservedFailedEventPayload;
import com.eric6166.order.dto.OrderDto;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.enums.OrderStatus;
import com.eric6166.order.model.Order;
import com.eric6166.order.service.OrderService;
import com.eric6166.order.utils.TestUtils;
import com.eric6166.security.utils.AppSecurityUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.validation.ConstraintViolationException;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
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
import java.util.UUID;

@WebMvcTest(controllers = {OrderController.class})
@FieldDefaults(level = AccessLevel.PRIVATE)
class OrderControllerTest {

    private static final String URL_TEMPLATE = "/order";
    private static OrderRequest.Item item;
    private static OrderRequest.Item item1;
    private static OrderRequest orderRequest;
    private static Order order;
    private static Order order1;
    private static OrderDto orderDto;
    private static OrderDto orderDto1;
    private static String username;
    private static MockedStatic<AppSecurityUtils> appSecurityUtilsMockedStatic;

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper objectMapper;
    @MockBean
    OrderService orderService;

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
        var uuid = UUID.randomUUID().toString();

        order = TestUtils.mockOrder(RandomUtils.nextLong(1, 100), uuid, username, OrderStatus.ORDER_CREATED, null, null);
        var orderDetail = TestUtils.mockOrderRequest(item, item1);
        order.setOrderDetail(objectMapper.writeValueAsString(orderDetail));
        orderDto = TestUtils.mockOrderDto(order, orderDetail);

        order1 = TestUtils.mockOrder(RandomUtils.nextLong(101, 200), uuid, username, OrderStatus.INVENTORY_RESERVED, null, null);
        var inventoryReservedItem = TestUtils.mockInventoryReservedItem(item, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var inventoryReservedItem1 = TestUtils.mockInventoryReservedItem(item1, BigDecimal.valueOf(RandomUtils.nextDouble(1, 10000)));
        var orderDetail1 = TestUtils.mockInventoryReservedEventPayload(uuid, username, inventoryReservedItem, inventoryReservedItem1);
        order1.setOrderDetail(objectMapper.writeValueAsString(orderDetail1));
        orderDto1 = TestUtils.mockOrderDto(order1, orderDetail1);
    }

    @Test
    void placeOrderKafka_thenReturnOk() throws Exception {
        var messageResponse = MessageResponse.builder()
                .uuid(UUID.randomUUID().toString())
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
        var uuid = order.getUuid();
        var expected = new AppResponse<>(orderDto);

        Mockito.when(orderService.getOrderByUuidAndUsername(uuid, username)).thenReturn(orderDto);

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
        var uuid = order.getUuid();

        var orderDtoList = List.of(orderDto1, orderDto);
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
        var uuid = UUID.randomUUID().toString();
        var order2 = TestUtils.mockOrder(RandomUtils.nextLong(201, 300), uuid, username, OrderStatus.INVENTORY_RESERVED_FAILED, null, null);
        var inventoryReservedFailedItem = TestUtils.mockInventoryReservedFailedItem(item, RandomUtils.nextInt(0, item.getOrderQuantity() - 1));
        var inventoryReservedFailedItem1 = TestUtils.mockInventoryReservedFailedItem(item1, null);
        var orderDetail2 = InventoryReservedFailedEventPayload.builder()
                .orderUuid(uuid)
                .username(username)
                .itemList(List.of(inventoryReservedFailedItem, inventoryReservedFailedItem1))
                .build();
        order2.setOrderDetail(objectMapper.writeValueAsString(orderDetail2));
        var orderDto2 = TestUtils.mockOrderDto(order2, orderDetail2);
        var pageNumber = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_NUMBER, BaseConst.DEFAULT_MAX_INTEGER);
        var pageSize = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_SIZE, BaseConst.MAXIMUM_PAGE_SIZE);
        var pageable = PageUtils.buildSimplePageable(pageNumber, pageSize);
        var orderDtoList = List.of(orderDto2, orderDto1);
        var pageResponse = new PageResponse<>(orderDtoList, new PageImpl<>(orderDtoList, pageable, orderDtoList.size()));
        var expected = new AppResponse<>(pageResponse);

        Mockito.when(orderService.getOrderHistoryByUsername(username, pageNumber, pageSize)).thenReturn(pageResponse);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_TEMPLATE + "/" + username + "/history")
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("pageNumber", String.valueOf(pageNumber))
                        .param("pageSize", String.valueOf(pageSize)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().json(objectMapper.writeValueAsString(expected)));

    }

    @Test
    void getOrderHistoryByUsername_givenDataHasNoContent_thenReturnNoContent() throws Exception {
        var pageNumber = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_NUMBER, BaseConst.DEFAULT_MAX_INTEGER);
        var pageSize = RandomUtils.nextInt(BaseConst.DEFAULT_PAGE_SIZE, BaseConst.MAXIMUM_PAGE_SIZE);
        var pageable = PageUtils.buildSimplePageable(pageNumber, pageSize);
        List<OrderDto> orderDtoList = new ArrayList<>();
        var pageResponse = new PageResponse<>(orderDtoList, new PageImpl<>(orderDtoList, pageable, orderDtoList.size()));

        Mockito.when(orderService.getOrderHistoryByUsername(username, pageNumber, pageSize)).thenReturn(pageResponse);

        mvc.perform(MockMvcRequestBuilders
                        .get(URL_TEMPLATE + "/" + username + "/history")
                        .with(SecurityMockMvcRequestPostProcessors
                                .jwt()
                                .authorities(new SimpleGrantedAuthority(TestConst.ROLE_CUSTOMER)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                        .characterEncoding(StandardCharsets.UTF_8)
                        .param("pageNumber", String.valueOf(pageNumber))
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