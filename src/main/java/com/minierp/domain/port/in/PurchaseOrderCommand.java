package com.minierp.domain.port.in;

import java.time.LocalDate;
import java.util.List;

/**
 * Purchase Order data for the create use case.
 */
public record PurchaseOrderCommand(Long supplierId, LocalDate orderDate, String notes,
        List<PurchaseOrderLineCommand> lines) {

    public PurchaseOrderCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
