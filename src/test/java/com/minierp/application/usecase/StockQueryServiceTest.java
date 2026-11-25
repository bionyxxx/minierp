package com.minierp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minierp.domain.model.inventory.Stock;
import com.minierp.domain.model.inventory.StockMovement;
import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.inventory.StockMovementType;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.InvalidSortException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.shared.PageQuery;
import com.minierp.domain.model.shared.PageResult;
import com.minierp.domain.model.shared.SortField;
import com.minierp.domain.model.user.Role;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.StockMovementRepositoryPort;
import com.minierp.domain.port.out.StockRepositoryPort;
import com.minierp.infrastructure.web.inventory.dto.ItemStockResponse;
import com.minierp.infrastructure.web.inventory.dto.StockMovementResponse;
import com.minierp.infrastructure.web.inventory.dto.StockResponse;

@ExtendWith(MockitoExtension.class)
class StockQueryServiceTest {

    @Mock
    private StockRepositoryPort stockRepository;

    @Mock
    private StockMovementRepositoryPort stockMovementRepository;

    @Mock
    private ItemRepositoryPort itemRepository;

    @InjectMocks
    private StockQueryService stockQueryService;

    private final AuthenticatedUser actor = new AuthenticatedUser("admin", Role.ADMIN);

    @Test
    void listStockMapsItemAndWarehouseDetails() {
        Item item = item();
        Warehouse warehouse = warehouse();
        when(stockRepository.findPage(eq(10L), eq(20L), any()))
                .thenReturn(new PageResult<>(List.of(Stock.reconstitute(1L, item, warehouse,
                        new BigDecimal("12.500"))), 0, 20, 1, 1));

        PageResult<Stock> result = stockQueryService.listStock(actor, 10L, 20L, new PageQuery(0, 20, List.of()));

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(0, result.page());
        StockResponse response = StockResponse.from(result.content().get(0));
        assertEquals(10L, response.itemId().longValue());
        assertEquals("SKU-001", response.sku());
        assertEquals("Widget", response.itemName());
        assertEquals(20L, response.warehouseId().longValue());
        assertEquals("WH-01", response.warehouseCode());
        assertEquals("Gudang Utama", response.warehouseName());
        assertEquals(0, new BigDecimal("12.500").compareTo(response.qty()));
    }

    @Test
    void getItemStockSumsWarehouseQuantities() {
        Item item = item();
        Warehouse warehouse = warehouse();
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        when(stockRepository.findAll(10L, null)).thenReturn(List.of(
                Stock.reconstitute(1L, item, warehouse, new BigDecimal("3.000")),
                Stock.reconstitute(2L, item, warehouse, new BigDecimal("4.500"))));

        ItemStockResponse response = ItemStockResponse.from(stockQueryService.getItemStock(actor, 10L));

        assertEquals(10L, response.itemId().longValue());
        assertEquals("SKU-001", response.sku());
        assertEquals("Widget", response.name());
        assertEquals("PCS", response.uom());
        assertEquals(0, new BigDecimal("7.500").compareTo(response.totalQty()));
        assertEquals(2, response.warehouseStocks().size());
    }

    @Test
    void getItemStockThrowsNotFoundForUnknownItem() {
        when(itemRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> stockQueryService.getItemStock(actor, 99L));
    }

    @Test
    void listMovementsMapsStockCardEntry() {
        Item item = item();
        Warehouse warehouse = warehouse();
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        StockMovement movement = StockMovement.reconstitute(5L, item, warehouse, StockMovementType.IN,
                new BigDecimal("5.000"), StockMovementRef.GRN, 1L, "GRN-0001", "terima barang", "admin", createdAt);
        when(stockMovementRepository.findPage(eq(10L), isNull(), any()))
                .thenReturn(new PageResult<>(List.of(movement), 0, 20, 1, 1));

        PageResult<StockMovement> result =
                stockQueryService.listMovements(actor, 10L, null, new PageQuery(0, 20, List.of()));

        assertEquals(1, result.content().size());
        StockMovementResponse response = StockMovementResponse.from(result.content().get(0));
        assertEquals(5L, response.id().longValue());
        assertEquals(10L, response.itemId().longValue());
        assertEquals("SKU-001", response.sku());
        assertEquals("Widget", response.itemName());
        assertEquals(20L, response.warehouseId().longValue());
        assertEquals("WH-01", response.warehouseCode());
        assertEquals(StockMovementType.IN, response.movementType());
        assertEquals(0, new BigDecimal("5.000").compareTo(response.qty()));
        assertEquals(StockMovementRef.GRN, response.refType());
        assertEquals("GRN-0001", response.refNumber());
        assertEquals("terima barang", response.remark());
        assertEquals("admin", response.createdBy());
        assertEquals(createdAt, response.createdAt());
    }

    @Test
    void listStockRejectsUnknownSortProperty() {
        assertThrows(InvalidSortException.class,
                () -> stockQueryService.listStock(actor, 10L, 20L,
                        new PageQuery(0, 20, List.of(SortField.asc("hacker")))));
    }

    @Test
    void listMovementsRejectsUnknownSortProperty() {
        assertThrows(InvalidSortException.class,
                () -> stockQueryService.listMovements(actor, 10L, null,
                        new PageQuery(0, 20, List.of(SortField.asc("hacker")))));
    }

    private static Item item() {
        return Item.reconstitute(10L, "SKU-001", "Widget", "PCS", BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, true, null, null);
    }

    private static Warehouse warehouse() {
        return Warehouse.reconstitute(20L, "WH-01", "Gudang Utama", null, true, null, null);
    }
}
