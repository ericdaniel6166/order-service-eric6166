package com.eric6166.order.controller;

import com.eric6166.base.exception.AppNotFoundException;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@Validated
@RequestMapping("/order")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/place-order-kafka")
    public ResponseEntity<Object> placeOrderKafka(@RequestBody OrderRequest request) throws JsonProcessingException {
        return ResponseEntity.ok(orderService.placeOrderKafka(request));
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{uuid}/status")
    public ResponseEntity<Object> getOrderStatusByUuid(@PathVariable String uuid) throws AppNotFoundException, JsonProcessingException {
        return ResponseEntity.ok(orderService.getOrderStatusByUuid(uuid));
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{uuid}/history")
    public ResponseEntity<Object> getOrderHistoryByUuid(@PathVariable String uuid) throws AppNotFoundException, JsonProcessingException {
        return ResponseEntity.ok(orderService.getOrderHistoryByUuid(uuid));
    }


}
