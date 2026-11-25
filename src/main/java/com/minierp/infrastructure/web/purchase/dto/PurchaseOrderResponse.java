package com.minierp.infrastructure.web.purchase.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.shared.DocumentStatus;

public record PurchaseOrderResponse(
        Long id,
        String poNumber,
        Long supplierId,
        String supplierName,
        LocalDate orderDate,
        DocumentStatus status,
        BigDecimal totalAmount,
        String notes,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt,
        String rejectReason,
        List<PurchaseOrderLineResponse> lines) {

    public static PurchaseOrderResponse from(PurchaseOrder po) {
        return new PurchaseOrderResponse(
                po.getId(),
                po.getPoNumber(),
                po.getSupplier().getId(),
                po.getSupplier().getName(),
                po.getOrderDate(),
                po.getStatus(),
                po.getTotalAmount(),
                po.getNotes(),
                po.getCreatedBy(),
                po.getCreatedAt(),
                po.getApprovedBy(),
                po.getApprovedAt(),
                po.getRejectReason(),
                po.getLines().stream().map(PurchaseOrderLineResponse::from).toList());
    }
}
