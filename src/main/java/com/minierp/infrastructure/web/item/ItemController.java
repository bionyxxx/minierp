package com.minierp.infrastructure.web.item;

import java.net.URI;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.minierp.application.support.Pagination;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.port.in.ItemUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.item.dto.ItemRequest;
import com.minierp.infrastructure.web.item.dto.ItemResponse;
import com.minierp.infrastructure.web.support.PageResponse;

import jakarta.validation.Valid;

/**
 * Item master data adapter.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemUseCase itemUseCase;
    private final CurrentUser currentUser;

    public ItemController(ItemUseCase itemUseCase, CurrentUser currentUser) {
        this.itemUseCase = itemUseCase;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<ItemResponse> list(@RequestParam(required = false) Boolean active, Pageable pageable) {
        PageResult<Item> page = itemUseCase.list(currentUser.get(), active, Pagination.toQuery(pageable));
        return PageResponse.from(page.map(ItemResponse::from));
    }

    @GetMapping("/{id}")
    public ItemResponse get(@PathVariable Long id) {
        return ItemResponse.from(itemUseCase.get(currentUser.get(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<ItemResponse> create(@Valid @RequestBody ItemRequest request) {
        ItemResponse response = ItemResponse.from(itemUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/items/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ItemResponse update(@PathVariable Long id, @Valid @RequestBody ItemRequest request) {
        return ItemResponse.from(itemUseCase.update(currentUser.get(), id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        itemUseCase.delete(currentUser.get(), id);
    }
}
