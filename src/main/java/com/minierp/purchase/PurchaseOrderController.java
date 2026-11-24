package com.minierp.purchase;

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

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.CurrentUser;
import com.minierp.common.DocumentStatus;
import com.minierp.common.PageResponse;
import com.minierp.purchase.dto.PurchaseOrderRequest;
import com.minierp.purchase.dto.PurchaseOrderResponse;
import com.minierp.purchase.dto.RejectRequest;

import jakarta.validation.Valid;

/**
 * HTTP adapter for Purchase Order use cases.
 */
@RestController
@RequestMapping("/api/purchase-orders")
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;
    private final CurrentUser currentUser;

    public PurchaseOrderController(PurchaseOrderService purchaseOrderService, CurrentUser currentUser) {
        this.purchaseOrderService = purchaseOrderService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<PurchaseOrderResponse> create(@Valid @RequestBody PurchaseOrderRequest request) {
        AuthenticatedUser actor = currentUser.get();
        PurchaseOrderResponse response = purchaseOrderService.create(actor, request);
        return ResponseEntity.created(URI.create("/api/purchase-orders/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<PurchaseOrderResponse> list(
            @RequestParam(required = false) DocumentStatus status,
            @RequestParam(required = false) Long supplierId, Pageable pageable) {
        return purchaseOrderService.list(currentUser.get(), status, supplierId, pageable);
    }

    @GetMapping("/{id}")
    public PurchaseOrderResponse get(@PathVariable Long id) {
        return purchaseOrderService.get(currentUser.get(), id);
    }

    @PostMapping("/{id}/approve")
    public PurchaseOrderResponse approve(@PathVariable Long id) {
        return purchaseOrderService.approve(currentUser.get(), id);
    }

    @PostMapping("/{id}/reject")
    public PurchaseOrderResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return purchaseOrderService.reject(currentUser.get(), id, request);
    }
}
