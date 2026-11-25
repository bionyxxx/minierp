package com.minierp.domain.port.in;

import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Delivery (pengiriman barang) use cases offered by the application.
 */
public interface DeliveryUseCase {

    PageResult<Delivery> list(AuthenticatedUser actor, Long salesOrderId, DocumentStatus status, PageQuery query);

    Delivery get(AuthenticatedUser actor, Long id);

    Delivery create(AuthenticatedUser actor, DeliveryCommand command);

    Delivery approve(AuthenticatedUser actor, Long id);

    Delivery reject(AuthenticatedUser actor, Long id, String reason);
}
