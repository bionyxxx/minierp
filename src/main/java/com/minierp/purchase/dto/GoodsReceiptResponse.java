package com.minierp.purchase.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.common.DocumentStatus;

public record GoodsReceiptResponse(
        Long id,
        String grnNumber,
        Long purchaseOrderId,
        String poNumber,
        Long warehouseId,
        String warehouseCode,
        LocalDate receiptDate,
        DocumentStatus status,
        String notes,
        String createdBy,
        Instant createdAt,
        String approvedBy,
        Instant approvedAt,
        String rejectReason,
        List<GoodsReceiptLineResponse> lines) {
}
