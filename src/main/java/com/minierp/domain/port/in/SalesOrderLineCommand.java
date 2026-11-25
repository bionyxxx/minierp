package com.minierp.domain.port.in;

import java.math.BigDecimal;

/**
 * One line of a create-Sales-Order command.
 */
public record SalesOrderLineCommand(Long itemId, BigDecimal qty, BigDecimal unitPrice) {
}
