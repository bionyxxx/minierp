package com.minierp.infrastructure.web.purchase;

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
import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.in.PurchaseOrderUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.purchase.dto.PurchaseOrderRequest;
import com.minierp.infrastructure.web.purchase.dto.PurchaseOrderResponse;
import com.minierp.infrastructure.web.purchase.dto.RejectRequest;
import com.minierp.infrastructure.web.support.PageResponse;

import jakarta.validation.Valid;

/**
 * HTTP adapter for Purchase Order use cases.
 */
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderUseCase purchaseOrderUseCase;
    private final CurrentUser currentUser;

    public PurchaseOrderController(PurchaseOrderUseCase purchaseOrderUseCase, CurrentUser currentUser) {
        this.purchaseOrderUseCase = purchaseOrderUseCase;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request) {
        PurchaseOrderResponse response = PurchaseOrderResponse.from(
                purchaseOrderUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/purchase-orders/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<PurchaseOrderResponse> list(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Long supplierId, Pageable pageable) {
        PageResult<PurchaseOrder> page = purchaseOrderUseCase.list(currentUser.get(), status, supplierId,
                Pagination.toQuery(pageable));
        return PageResponse.from(page.map(PurchaseOrderResponse::from));
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return PurchaseOrderResponse.from(purchaseOrderUseCase.get(currentUser.get(), id));
    }

    @PostMapping("/{id}/approve")
    public PurchaseOrderResponse approve(@PathVariable Long id) {
        return PurchaseOrderResponse.from(purchaseOrderUseCase.approve(currentUser.get(), id));
    }

    @PostMapping("/{id}/reject")
    public PurchaseOrderResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return PurchaseOrderResponse.from(purchaseOrderUseCase.reject(currentUser.get(), id, request.reason()));
    }
}
