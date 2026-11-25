package com.minierp.domain.port.out;

import java.util.List;
import java.util.Optional;

import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: Goods Receipt persistence needed by the application.
 */
public interface GoodsReceiptRepositoryPort {

    Optional<GoodsReceipt> findById(Long id);

    List<GoodsReceipt> findByPurchaseOrderId(Long purchaseOrderId);

    GoodsReceipt save(GoodsReceipt goodsReceipt);

    PageResult<GoodsReceipt> findPage(Long purchaseOrderId, DocumentStatus status, PageQuery query);
}
