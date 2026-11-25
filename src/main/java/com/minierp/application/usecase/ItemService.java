package com.minierp.application.usecase;

import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.DuplicateResourceException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.port.in.ItemCommand;
import com.minierp.domain.port.in.ItemUseCase;
import com.minierp.domain.port.out.ItemRepositoryPort;

/**
 * Master data CRUD for items.
 */
@Service
public class ItemService implements ItemUseCase {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "sku", "name", "uom", "minStock", "purchasePrice", "sellingPrice", "active");

    private static final SortField DEFAULT_SORT = SortField.asc("sku");

    private final ItemRepositoryPort itemRepository;

    public ItemService(ItemRepositoryPort itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    @Override
    public PageResult<Item> list(AuthenticatedUser actor, Boolean active, PageQuery query) {
        return itemRepository.findPage(active, Pagination.normalize(query, SORT_PROPERTIES, DEFAULT_SORT));
    }

    @Transactional(readOnly = true)
    @Override
    public Item get(AuthenticatedUser actor, Long id) {
        return findOrThrow(id);
    }

    @Transactional
    @Override
    public Item create(AuthenticatedUser actor, ItemCommand command) {
        ensureSkuAvailable(command.sku());
        Item item = Item.create(command.sku(), command.name(), command.uom(),
                command.minStock(), command.purchasePrice(), command.sellingPrice());
        return itemRepository.save(item);
    }

    @Transactional
    @Override
    public Item update(AuthenticatedUser actor, Long id, ItemCommand command) {
        Item item = findOrThrow(id);
        if (!item.getSku().equals(command.sku())) {
            ensureSkuAvailable(command.sku());
        }
        item.update(command.sku(), command.name(), command.uom(),
                command.minStock(), command.purchasePrice(), command.sellingPrice());
        return itemRepository.save(item);
    }

    /**
     * Soft delete: the item stays referenced by documents but is deactivated.
     */
    @Transactional
    @Override
    public void delete(AuthenticatedUser actor, Long id) {
        Item item = findOrThrow(id);
        item.deactivate();
        itemRepository.save(item);
    }

    private Item findOrThrow(Long id) {
        return itemRepository.findById(id).orElseThrow(() -> new NotFoundException("Item", id));
    }

    private void ensureSkuAvailable(String sku) {
        if (itemRepository.existsBySku(sku)) {
            throw new DuplicateResourceException("SKU %s sudah digunakan".formatted(sku));
        }
    }
}
