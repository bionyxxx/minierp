package com.minierp.infrastructure.web.purchase.dto;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.shared.DocumentStatus;

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

    public static GoodsReceiptResponse from(GoodsReceipt grn) {
        return new GoodsReceiptResponse(
                grn.getId(),
                grn.getGrnNumber(),
                grn.getPurchaseOrder().getId(),
                grn.getPurchaseOrder().getPoNumber(),
                grn.getWarehouse().getId(),
                grn.getWarehouse().getCode(),
                grn.getReceiptDate(),
                grn.getStatus(),
                grn.getNotes(),
                grn.getCreatedBy(),
                grn.getCreatedAt(),
                grn.getApprovedBy(),
                grn.getApprovedAt(),
                grn.getRejectReason(),
                grn.getLines().stream().map(GoodsReceiptLineResponse::from).toList());
    }
}
