package com.minierp.domain.port.in;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Item master data use cases offered by the application.
 */
public interface ItemUseCase {

    PageResult<Item> list(AuthenticatedUser actor, Boolean active, PageQuery query);

    Item get(AuthenticatedUser actor, Long id);

    Item create(AuthenticatedUser actor, ItemCommand command);

    Item update(AuthenticatedUser actor, Long id, ItemCommand command);

    void delete(AuthenticatedUser actor, Long id);
}
