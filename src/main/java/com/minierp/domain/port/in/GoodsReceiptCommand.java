package com.minierp.domain.port.in;

import java.time.LocalDate;
import java.util.List;

/**
 * Goods Receipt data for the create use case.
 */
public record GoodsReceiptCommand(Long purchaseOrderId, Long warehouseId, LocalDate receiptDate, String notes,
        List<GoodsReceiptLineCommand> lines) {

    public GoodsReceiptCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
