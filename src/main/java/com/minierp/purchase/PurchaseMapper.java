package com.minierp.purchase;

import com.minierp.purchase.dto.GoodsReceiptLineResponse;
import com.minierp.purchase.dto.GoodsReceiptResponse;
import com.minierp.purchase.dto.PurchaseOrderLineResponse;
import com.minierp.purchase.dto.PurchaseOrderResponse;

/**
 * Entity to DTO mapping for the purchase slice. Kept package-private so the
 * controllers and services share one mapping implementation.
 */
final class PurchaseMapper {

    private PurchaseMapper() {
    }

    static PurchaseOrderResponse toResponse(PurchaseOrder po) {
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
                po.getLines().stream().map(PurchaseMapper::toLineResponse).toList());
    }

    static PurchaseOrderLineResponse toLineResponse(PurchaseOrderLine line) {
        return new PurchaseOrderLineResponse(
                line.getItem().getId(),
                line.getItem().getSku(),
                line.getItem().getName(),
                line.getQty(),
                line.getUnitPrice(),
                line.getLineTotal());
    }

    static GoodsReceiptResponse toResponse(GoodsReceipt grn) {
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
                grn.getLines().stream().map(PurchaseMapper::toLineResponse).toList());
    }

    static GoodsReceiptLineResponse toLineResponse(GoodsReceiptLine line) {
        return new GoodsReceiptLineResponse(
                line.getItem().getId(),
                line.getItem().getSku(),
                line.getItem().getName(),
                line.getQty());
    }
}
