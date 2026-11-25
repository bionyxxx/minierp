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
import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.in.DeliveryUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.sales.dto.DeliveryRequest;
import com.minierp.infrastructure.web.sales.dto.DeliveryResponse;
import com.minierp.infrastructure.web.sales.dto.RejectRequest;
import com.minierp.infrastructure.web.support.PageResponse;

import jakarta.validation.Valid;

/**
 * Delivery adapter.
 */
@RestController
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryUseCase deliveryUseCase;
    private final CurrentUser currentUser;

    public DeliveryController(DeliveryUseCase deliveryUseCase, CurrentUser currentUser) {
        this.deliveryUseCase = deliveryUseCase;
        this.currentUser = currentUser;
    }

    @PostMapping
    public ResponseEntity<DeliveryResponse> create(@Valid @RequestBody DeliveryRequest request) {
        DeliveryResponse response = DeliveryResponse.from(
                deliveryUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/deliveries/" + response.id())).body(response);
    }

    @GetMapping
    public PageResponse<DeliveryResponse> list(@RequestParam(required = false) Long salesOrderId,
            @RequestParam(required = false) DocumentStatus status, Pageable pageable) {
        PageResult<Delivery> page = deliveryUseCase.list(currentUser.get(), salesOrderId, status,
                Pagination.toQuery(pageable));
        return PageResponse.from(page.map(DeliveryResponse::from));
    }

    @GetMapping("/{id}")
    public DeliveryResponse get(@PathVariable Long id) {
        return DeliveryResponse.from(deliveryUseCase.get(currentUser.get(), id));
    }

    @PostMapping("/{id}/approve")
    public DeliveryResponse approve(@PathVariable Long id) {
        return DeliveryResponse.from(deliveryUseCase.approve(currentUser.get(), id));
    }

    @PostMapping("/{id}/reject")
    public DeliveryResponse reject(@PathVariable Long id, @Valid @RequestBody RejectRequest request) {
        return DeliveryResponse.from(deliveryUseCase.reject(currentUser.get(), id, request.reason()));
    }
}
