package com.minierp.inventory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.InvalidSortException;
import com.minierp.common.NotFoundException;
import com.minierp.common.PageResponse;
import com.minierp.inventory.dto.ItemStockResponse;
import com.minierp.inventory.dto.StockMovementResponse;
import com.minierp.inventory.dto.StockResponse;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.user.Role;
import com.minierp.warehouse.Warehouse;

@ExtendWith(MockitoExtension.class)
class StockQueryServiceTest {

    @Mock
    private StockRepository stockRepository;

    @Mock
    private StockMovementRepository stockMovementRepository;

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private StockQueryService stockQueryService;

    private final AuthenticatedUser actor = new AuthenticatedUser("admin", Role.ADMIN);

    @Test
    void listStockMapsItemAndWarehouseDetails() {
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(10L);
        when(item.getSku()).thenReturn("SKU-001");
        when(item.getName()).thenReturn("Widget");
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getId()).thenReturn(20L);
        when(warehouse.getCode()).thenReturn("WH-01");
        when(warehouse.getName()).thenReturn("Gudang Utama");
        when(stockRepository.findPageWithItemAndWarehouse(eq(10L), eq(20L), any()))
                .thenReturn(new PageImpl<>(List.of(new Stock(item, warehouse, new BigDecimal("12.500"))),
                        PageRequest.of(0, 20), 1));

        PageResponse<StockResponse> result = stockQueryService.listStock(actor, 10L, 20L, PageRequest.of(0, 20));

        assertEquals(1, result.content().size());
        assertEquals(1, result.totalElements());
        assertEquals(0, result.page());
        StockResponse response = result.content().get(0);
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
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(10L);
        when(item.getSku()).thenReturn("SKU-001");
        when(item.getName()).thenReturn("Widget");
        when(item.getUom()).thenReturn("PCS");
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));
        Warehouse warehouse = mock(Warehouse.class);
        when(stockRepository.findAllWithItemAndWarehouse(10L, null)).thenReturn(List.of(
                new Stock(item, warehouse, new BigDecimal("3.000")),
                new Stock(item, warehouse, new BigDecimal("4.500"))));

        ItemStockResponse response = stockQueryService.getItemStock(actor, 10L);

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
        Item item = mock(Item.class);
        when(item.getId()).thenReturn(10L);
        when(item.getSku()).thenReturn("SKU-001");
        when(item.getName()).thenReturn("Widget");
        Warehouse warehouse = mock(Warehouse.class);
        when(warehouse.getId()).thenReturn(20L);
        when(warehouse.getCode()).thenReturn("WH-01");
        Instant createdAt = Instant.parse("2026-01-01T00:00:00Z");
        StockMovement movement = mock(StockMovement.class);
        when(movement.getId()).thenReturn(5L);
        when(movement.getItem()).thenReturn(item);
        when(movement.getWarehouse()).thenReturn(warehouse);
        when(movement.getMovementType()).thenReturn(StockMovementType.IN);
        when(movement.getQty()).thenReturn(new BigDecimal("5.000"));
        when(movement.getRefType()).thenReturn(StockMovementRef.GRN);
        when(movement.getRefNumber()).thenReturn("GRN-0001");
        when(movement.getRemark()).thenReturn("terima barang");
        when(movement.getCreatedBy()).thenReturn("admin");
        when(movement.getCreatedAt()).thenReturn(createdAt);
        when(stockMovementRepository.findPageWithItemAndWarehouse(eq(10L), isNull(), any()))
                .thenReturn(new PageImpl<>(List.of(movement), PageRequest.of(0, 20), 1));

        PageResponse<StockMovementResponse> result =
                stockQueryService.listMovements(actor, 10L, null, PageRequest.of(0, 20));

        assertEquals(1, result.content().size());
        StockMovementResponse response = result.content().get(0);
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
                () -> stockQueryService.listStock(actor, 10L, 20L, PageRequest.of(0, 20, Sort.by("hacker"))));
    }

    @Test
    void listMovementsRejectsUnknownSortProperty() {
        assertThrows(InvalidSortException.class,
                () -> stockQueryService.listMovements(actor, 10L, null, PageRequest.of(0, 20, Sort.by("hacker"))));
    }
}
