package com.minierp.domain.port.in;

import java.time.LocalDate;
import java.util.List;

/**
 * Sales Order data for the create use case.
 */
public record SalesOrderCommand(Long customerId, LocalDate orderDate, String notes,
        List<SalesOrderLineCommand> lines) {

    public SalesOrderCommand {
        lines = lines == null ? List.of() : List.copyOf(lines);
    }
}
