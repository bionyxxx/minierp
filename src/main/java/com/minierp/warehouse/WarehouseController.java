package com.minierp.warehouse;

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

import com.minierp.common.CurrentUser;
import com.minierp.common.PageResponse;
import com.minierp.warehouse.dto.WarehouseRequest;
import com.minierp.warehouse.dto.WarehouseResponse;

import jakarta.validation.Valid;

/**
 * Warehouse master data adapter.
 */
@RestController
@RequestMapping("/api/warehouses")
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final CurrentUser currentUser;

    public WarehouseController(WarehouseService warehouseService, CurrentUser currentUser) {
        this.warehouseService = warehouseService;
        this.currentUser = currentUser;
    }

    @GetMapping
    public PageResponse<WarehouseResponse> list(@RequestParam(required = false) Boolean active, Pageable pageable) {
        return warehouseService.list(currentUser.get(), active, pageable);
    }

    @GetMapping("/{id}")
    public WarehouseResponse get(@PathVariable Long id) {
        return warehouseService.get(currentUser.get(), id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public ResponseEntity<WarehouseResponse> create(@Valid @RequestBody WarehouseRequest request) {
        WarehouseResponse response = warehouseService.create(currentUser.get(), request);
        return ResponseEntity.created(URI.create("/api/warehouses/" + response.id())).body(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    public WarehouseResponse update(@PathVariable Long id, @Valid @RequestBody WarehouseRequest request) {
        return warehouseService.update(currentUser.get(), id, request);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','MANAGER')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        warehouseService.delete(currentUser.get(), id);
    }
}
