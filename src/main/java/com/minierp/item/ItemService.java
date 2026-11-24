package com.minierp.item;

import java.math.BigDecimal;
import java.util.Set;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.DuplicateResourceException;
import com.minierp.common.NotFoundException;
import com.minierp.common.PageResponse;
import com.minierp.common.Pagination;
import com.minierp.item.dto.ItemRequest;
import com.minierp.item.dto.ItemResponse;

/**
 * Master data CRUD for items.
 */
@Service
public class ItemService {

    private static final Set<String> SORT_PROPERTIES =
            Set.of("id", "sku", "name", "uom", "minStock", "purchasePrice", "sellingPrice", "active");

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ItemResponse> list(AuthenticatedUser actor, Boolean active, Pageable pageable) {
        Pageable effective = Pagination.normalize(pageable, SORT_PROPERTIES, Sort.by("sku"));
        return PageResponse.from(itemRepository.findPage(active, effective).map(ItemResponse::from));
    }

    @Transactional(readOnly = true)
    public ItemResponse get(AuthenticatedUser actor, Long id) {
        return ItemResponse.from(findOrThrow(id));
    }

    @Transactional
    public ItemResponse create(AuthenticatedUser actor, ItemRequest request) {
        ensureSkuAvailable(request.sku());
        Item item = new Item();
        apply(item, request);
        return ItemResponse.from(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(AuthenticatedUser actor, Long id, ItemRequest request) {
        Item item = findOrThrow(id);
        if (!item.getSku().equals(request.sku())) {
            ensureSkuAvailable(request.sku());
        }
        apply(item, request);
        return ItemResponse.from(itemRepository.save(item));
    }

    /**
     * Soft delete: the item stays referenced by documents but is deactivated.
     */
    @Transactional
    public void delete(AuthenticatedUser actor, Long id) {
        Item item = findOrThrow(id);
        item.setActive(false);
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

    private void apply(Item item, ItemRequest request) {
        item.setSku(request.sku());
        item.setName(request.name());
        item.setUom(request.uom());
        item.setMinStock(orZero(request.minStock()));
        item.setPurchasePrice(orZero(request.purchasePrice()));
        item.setSellingPrice(orZero(request.sellingPrice()));
    }

    private BigDecimal orZero(BigDecimal value) {
        return value != null ? value : BigDecimal.ZERO;
    }
}
