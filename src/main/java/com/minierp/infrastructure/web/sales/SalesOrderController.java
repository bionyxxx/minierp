package com.minierp.infrastructure.web.sales;

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

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.in.SalesOrderUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.sales.dto.RejectRequest;
import com.minierp.infrastructure.web.sales.dto.SalesOrderRequest;
import com.minierp.infrastructure.web.sales.dto.SalesOrderResponse;
import com.minierp.infrastructure.web.support.PageResponse;

import jakarta.validation.Valid;

/**
 * Sales Order adapter.
 */
@RestController
@RequestMapping("/api/sales-orders")
public class SalesOrderController {

    private final SalesOrderUseCase salesOrderUseCase;
    private final CurrentUser currentUser;

    public SalesOrderController(SalesOrderUseCase salesOrderUseCase, CurrentUser currentUser) {
        this.salesOrderUseCase = salesOrderUseCase;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<SalesOrderResponse> create(@Valid @RequestBody SalesOrderRequest request) {
        SalesOrderResponse response = SalesOrderResponse.from(
                salesOrderUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/sales-orders/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<SalesOrderResponse> list(@RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Long customerId, Pageable pageable) {
        PageResult<SalesOrder> page = salesOrderUseCase.list(currentUser.get(), status, customerId,
                Pagination.toQuery(pageable));
        return PageResponse.from(page.map(SalesOrderResponse::from));
    }

    @GetMapping("/{id}")
    public SalesOrderResponse get(@PathVariable Long id) {
        return SalesOrderResponse.from(salesOrderUseCase.get(currentUser.get(), id));
    }

    @PostMapping("/{id}/approve")
    public SalesOrderResponse approve(@PathVariable Long id) {
        return SalesOrderResponse.from(salesOrderUseCase.approve(currentUser.get(), id));
    }

    @PostMapping("/{id}/reject")
    public SalesOrderResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return SalesOrderResponse.from(salesOrderUseCase.reject(currentUser.get(), id, request.reason()));
    }
}
