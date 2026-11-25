package com.minierp.infrastructure.web.warehouse;

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
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.in.WarehouseUseCase;
import com.minierp.infrastructure.security.CurrentUser;
import com.minierp.infrastructure.web.support.PageResponse;
import com.minierp.infrastructure.web.warehouse.dto.WarehouseRequest;
import com.minierp.infrastructure.web.warehouse.dto.WarehouseResponse;

import jakarta.validation.Valid;

/**
 * Warehouse master data adapter.
 */
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseUseCase warehouseUseCase;
    private final CurrentUser currentUser;

    public WarehouseController(WarehouseUseCase warehouseUseCase, CurrentUser currentUser) {
        this.warehouseUseCase = warehouseUseCase;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<WarehouseResponse> list(@RequestParam(required = false) Boolean active, Pageable pageable) {
        PageResult<Warehouse> page = warehouseUseCase.list(currentUser.get(), active, Pagination.toQuery(pageable));
        return PageResponse.from(page.map(WarehouseResponse::from));
    }

    @GetMapping("/{id}")
    public WarehouseResponse get(@PathVariable Long id) {
        return WarehouseResponse.from(warehouseUseCase.get(currentUser.get(), id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse response = WarehouseResponse.from(warehouseUseCase.create(currentUser.get(), request.toCommand()));
        return ResponseEntity.created(URI.create("/api/warehouses/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public WarehouseResponse update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        return WarehouseResponse.from(warehouseUseCase.update(currentUser.get(), id, request.toCommand()));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        warehouseUseCase.delete(currentUser.get(), id);
    }
}
