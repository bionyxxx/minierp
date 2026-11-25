package com.minierp.infrastructure.web.purchase.dto;

import java.time.LocalDate;
import java.util.List;

import com.minierp.domain.port.in.PurchaseOrderCommand;
import com.minierp.domain.port.in.PurchaseOrderLineCommand;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

public record PurchaseOrderRequest(
        @NotNull(message = "supplierId wajib diisi") Long supplierId,
        @NotNull(message = "orderDate wajib diisi") LocalDate orderDate,
        String notes,
        @NotEmpty(message = "Purchase Order minimal memiliki 1 line")
        List<@Valid PurchaseOrderLineRequest> lines) {

    public PurchaseOrderCommand toCommand() {
        List<PurchaseOrderLineCommand> lineCommands = lines == null ? List.of() : lines.stream()
                .map(line -> new PurchaseOrderLineCommand(line.itemId(), line.qty(), line.unitPrice()))
                .toList();
        return new PurchaseOrderCommand(supplierId, orderDate, notes, lineCommands);
    }
}
