package com.minierp.sales;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.common.CurrentUser;
import com.minierp.common.DocumentStatus;
import com.minierp.common.PageResponse;
import com.minierp.sales.dto.RejectRequest;
import com.minierp.sales.dto.SalesOrderRequest;
import com.minierp.sales.dto.SalesOrderResponse;

import jakarta.validation.Valid;

/**
 * Sales Order adapter.
 */
@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderService salesOrderService;
    private final CurrentUser currentUser;

    public SalesOrderController(SalesOrderService salesOrderService, CurrentUser currentUser) {
        this.salesOrderService = salesOrderService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody SalesOrderRequest request) {
        SalesOrderResponse response = salesOrderService.create(currentUser.get(), request);
        return ResponseEntity.created(URI.create("/api/sales-orders/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<SalesOrderResponse> list(@RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Long customerId, Pageable pageable) {
        return salesOrderService.list(status, customerId, pageable);
    }

    @GetMapping("/{id}")
    public SalesOrderResponse get(@PathVariable Long id) {
        return salesOrderService.getById(id);
    }

    @PostMapping("/{id}/approve")
    public SalesOrderResponse approve(@PathVariable Long id) {
        return salesOrderService.approve(currentUser.get(), id);
    }

    @PostMapping("/{id}/reject")
    public SalesOrderResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return salesOrderService.reject(currentUser.get(), id, request);
    }
}
