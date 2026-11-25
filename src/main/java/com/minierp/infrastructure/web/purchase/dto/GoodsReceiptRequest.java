package com.minierp.infrastructure.web.purchase.dto;

import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.port.in.GoodsReceiptCommand;
import com.minierp.domain.port.in.GoodsReceiptLineCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record GoodsReceiptRequest(
        @NotNull(message = "purchaseOrderId wajib diisi") Long purchaseOrderId,
        @NotNull(message = "warehouseId wajib diisi") Long warehouseId,
        @NotNull(message = "receiptDate wajib diisi") LocalDate receiptDate,
        String notes,
        @NotEmpty(message = "Goods Receipt minimal memiliki 1 line")
        List<@Valid GoodsReceiptLineRequest> lines) {

    public GoodsReceiptCommand toCommand() {
        List<GoodsReceiptLineCommand> lineCommands = lines == null ? List.of() : lines.stream()
                .map(line -> new GoodsReceiptLineCommand(line.itemId(), line.qty()))
                .toList();
        return new GoodsReceiptCommand(purchaseOrderId, warehouseId, receiptDate, notes, lineCommands);
    }
}
