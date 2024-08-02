package com.eric6166.order.controller;

import com.eric6166.base.dto.AppResponse;
import com.eric6166.base.dto.MessageResponse;
import com.eric6166.base.exception.AppException;
import com.eric6166.base.utils.BaseConst;
import com.eric6166.base.validation.ValidString;
import com.eric6166.jpa.dto.PageResponse;
import com.eric6166.order.dto.OrderRequest;
import com.eric6166.order.dto.OrderResponse;
import com.eric6166.order.service.OrderService;
import com.fasterxml.jackson.core.JsonProcessingException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Slf4j
@Validated
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/place-order-kafka")
    public ResponseEntity<AppResponse<MessageResponse>> placeOrderKafka(@RequestBody OrderRequest request)
            throws JsonProcessingException {
        return ResponseEntity.ok(new AppResponse<>(orderService.placeOrderKafka(request)));
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/place-order-mqtt")
    public ResponseEntity<AppResponse<MessageResponse>> placeOrderMqtt(@RequestBody OrderRequest request) {
        return ResponseEntity.ok(new AppResponse<>(orderService.placeOrderMqtt(request)));
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{username}/uuid/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public ResponseEntity<AppResponse<OrderResponse>> getOrderByUuidAndUsername(@PathVariable String username, @PathVariable String uuid)
            throws AppException, JsonProcessingException {
        return ResponseEntity.ok(new AppResponse<>(orderService.getOrderByUuidAndUsername(uuid, username)));
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{username}/history/uuid/{uuid}")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public ResponseEntity<AppResponse<List<OrderResponse>>> getOrderHistoryByUuidAndUsername(@PathVariable String username, @PathVariable String uuid)
            throws AppException {
        return ResponseEntity.ok(new AppResponse<>(orderService.getOrderHistoryByUuidAndUsername(uuid, username)));
    }

    @Operation(security = @SecurityRequirement(name = "bearerAuth"))
    @GetMapping("/{username}/history")
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public ResponseEntity<AppResponse<PageResponse<OrderResponse>>> getOrderHistoryByUsername(
            @PathVariable String username,
            @RequestParam(required = false, defaultValue = BaseConst.PREVIOUS_30_DAYS)
            @ValidString(values = {
                    BaseConst.PREVIOUS_30_DAYS,
                    BaseConst.PREVIOUS_60_DAYS,
                    BaseConst.PREVIOUS_90_DAYS}) String days,
            @RequestParam(required = false, defaultValue = BaseConst.DEFAULT_PAGE_NUMBER_STRING)
            @Min(value = BaseConst.DEFAULT_PAGE_NUMBER)
            @Max(value = BaseConst.DEFAULT_MAX_INTEGER) Integer pageNumber,
            @RequestParam(required = false, defaultValue = BaseConst.DEFAULT_PAGE_SIZE_STRING)
            @Min(value = BaseConst.DEFAULT_PAGE_SIZE)
            @Max(value = BaseConst.MAXIMUM_PAGE_SIZE) Integer pageSize
    ) {
        var data = orderService.getOrderHistoryByUsername(username, Integer.parseInt(days), pageNumber, pageSize);
        if (!data.getPageable().isHasContent()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(new AppResponse<>(data));
    }


}
