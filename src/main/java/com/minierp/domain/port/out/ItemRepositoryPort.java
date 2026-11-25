package com.minierp.domain.port.out;

import java.util.Optional;

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;

/**
 * Driven port: item persistence needed by the application.
 */
public interface ItemRepositoryPort {

    Optional<Item> findById(Long id);

    Optional<Item> findBySku(String sku);

    boolean existsBySku(String sku);

    Item save(Item item);

    PageResult<Item> findPage(Boolean active, PageQuery query);
}
