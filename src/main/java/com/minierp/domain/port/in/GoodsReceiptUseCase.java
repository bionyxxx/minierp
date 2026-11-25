package com.minierp.domain.port.in;

import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Goods Receipt (penerimaan barang) use cases offered by the application.
 */
public interface GoodsReceiptUseCase {

    PageResult<GoodsReceipt> list(AuthenticatedUser actor, Long purchaseOrderId, DocumentStatus status,
            PageQuery query);

    GoodsReceipt get(AuthenticatedUser actor, Long id);

    GoodsReceipt create(AuthenticatedUser actor, GoodsReceiptCommand command);

    GoodsReceipt approve(AuthenticatedUser actor, Long id);

    GoodsReceipt reject(AuthenticatedUser actor, Long id, String reason);
}
