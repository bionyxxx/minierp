package com.minierp.domain.port.in;

import java.math.BigDecimal;

/**
 * One line of a create-Goods-Receipt command.
 */
public record GoodsReceiptLineCommand(Long itemId, BigDecimal qty) {
}
