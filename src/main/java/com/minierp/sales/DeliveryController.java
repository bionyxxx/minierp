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
import com.minierp.sales.dto.DeliveryRequest;
import com.minierp.sales.dto.DeliveryResponse;
import com.minierp.sales.dto.RejectRequest;

import jakarta.validation.Valid;

/**
 * Delivery adapter.
 */
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;
    private final CurrentUser currentUser;

    public DeliveryController(DeliveryService deliveryService, CurrentUser currentUser) {
        this.deliveryService = deliveryService;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody DeliveryRequest request) {
        DeliveryResponse response = deliveryService.create(currentUser.get(), request);
        return ResponseEntity.created(URI.create("/api/deliveries/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<DeliveryResponse> list(@RequestParam(required = false) Long salesOrderId,
            @RequestParam(required = false) DocumentStatus status, Pageable pageable) {
        return deliveryService.list(salesOrderId, status, pageable);
    }

    @GetMapping("/{id}")
    public DeliveryResponse get(@PathVariable Long id) {
        return deliveryService.getById(id);
    }

    @PostMapping("/{id}/approve")
    public DeliveryResponse approve(@PathVariable Long id) {
        return deliveryService.approve(currentUser.get(), id);
    }

    @PostMapping("/{id}/reject")
    public DeliveryResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return deliveryService.reject(currentUser.get(), id, request);
    }
}
