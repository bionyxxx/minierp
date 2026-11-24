package com.minierp.purchase.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.common.DocumentStatus;

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
}
