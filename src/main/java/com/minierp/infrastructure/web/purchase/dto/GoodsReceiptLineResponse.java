package com.minierp.infrastructure.web.purchase.dto;

import java.math.BigDecimal;

import com.minierp.domain.model.purchase.GoodsReceiptLine;

public record GoodsReceiptLineResponse(
        Long itemId,
        String sku,
        String itemName,
        BigDecimal qty) {

    public static GoodsReceiptLineResponse from(GoodsReceiptLine line) {
        return new GoodsReceiptLineResponse(line.getItem().getId(), line.getItem().getSku(),
                line.getItem().getName(), line.getQty());
    }
}
