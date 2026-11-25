package com.minierp.infrastructure.web.purchase.dto;

import java.math.BigDecimal;

import com.minierp.domain.model.purchase.PurchaseOrderLine;

public record PurchaseOrderLineResponse(
        Long itemId,
        String sku,
        String itemName,
        BigDecimal qty,
        BigDecimal unitPrice,
        BigDecimal lineTotal) {

    public static PurchaseOrderLineResponse from(PurchaseOrderLine line) {
        return new PurchaseOrderLineResponse(line.getItem().getId(), line.getItem().getSku(),
                line.getItem().getName(), line.getQty(), line.getUnitPrice(), line.getLineTotal());
    }
}
