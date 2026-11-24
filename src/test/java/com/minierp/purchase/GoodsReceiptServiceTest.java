package com.minierp.purchase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.minierp.approval.ApprovalPolicy;
import com.minierp.common.AuthenticatedUser;
import com.minierp.common.BusinessRuleException;
import com.minierp.common.DocumentStatus;
import com.minierp.common.InvalidStateException;
import com.minierp.config.MinierpProperties;
import com.minierp.inventory.StockMovementRef;
import com.minierp.inventory.StockService;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.purchase.dto.GoodsReceiptLineRequest;
import com.minierp.purchase.dto.GoodsReceiptRequest;
import com.minierp.purchase.dto.GoodsReceiptResponse;
import com.minierp.purchase.dto.RejectRequest;
import com.minierp.supplier.Supplier;
import com.minierp.warehouse.Warehouse;
import com.minierp.warehouse.WarehouseRepository;
import com.minierp.user.Role;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    private static final AuthenticatedUser STAFF = new AuthenticatedUser("budi", Role.STAFF);
    private static final AuthenticatedUser MANAGER = new AuthenticatedUser("maya", Role.MANAGER);

    @Mock
    private GoodsReceiptRepository goodsReceiptRepository;

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockService stockService;

    private GoodsReceiptService service;

    @BeforeEach
    void setUp() {
        ApprovalPolicy approvalPolicy = new ApprovalPolicy(new MinierpProperties(
                new MinierpProperties.Security(new MinierpProperties.Jwt("secret", Duration.ofHours(8))),
                new MinierpProperties.Approval(10000000),
                new MinierpProperties.Seed(false),
                new MinierpProperties.Api("https://minierp.local/problems/")));
        service = new GoodsReceiptService(goodsReceiptRepository, purchaseOrderRepository, warehouseRepository,
                itemRepository, stockService, approvalPolicy);
    }

    @Test
    void create_onPendingPoThrowsInvalidStateException() {
        PurchaseOrder po = approvedPo(item(10L, "SKU-A", "Item A"), new BigDecimal("10"));
        po.setStatus(DocumentStatus.PENDING);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        GoodsReceiptRequest request = new GoodsReceiptRequest(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineRequest(10L, BigDecimal.ONE)));

        assertThrows(InvalidStateException.class, () -> service.create(STAFF, request));
    }

    @Test
    void create_computesGrnNumberAndLineMembership() {
        Item itemA = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(itemA));
        when(goodsReceiptRepository.findByPurchaseOrderId(1L)).thenReturn(List.of());
        when(goodsReceiptRepository.nextNumberValue()).thenReturn(1L);
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(invocation -> {
            GoodsReceipt saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        GoodsReceiptRequest request = new GoodsReceiptRequest(1L, 2L, LocalDate.of(2026, 2, 1), "diterima lengkap",
                List.of(new GoodsReceiptLineRequest(10L, new BigDecimal("4"))));

        GoodsReceiptResponse response = service.create(STAFF, request);

        assertEquals("GRN-000001", response.grnNumber());
        assertEquals("PO-000001", response.poNumber());
        assertEquals(2L, response.warehouseId().longValue());
        assertEquals("WH-01", response.warehouseCode());
        assertEquals(DocumentStatus.PENDING, response.status());
        assertEquals("budi", response.createdBy());
        assertEquals(1, response.lines().size());
        assertEquals(0, new BigDecimal("4").compareTo(response.lines().get(0).qty()));
        assertEquals("SKU-A", response.lines().get(0).sku());
    }

    @Test
    void create_itemNotInPoThrowsBusinessRuleException() {
        Item itemA = item(10L, "SKU-A", "Item A");
        Item itemB = item(11L, "SKU-B", "Item B");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(11L)).thenReturn(Optional.of(itemB));

        GoodsReceiptRequest request = new GoodsReceiptRequest(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineRequest(11L, BigDecimal.ONE)));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, request));
    }

    @Test
    void create_cumulativeQtyOverflowThrowsBusinessRuleException() {
        Item itemA = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt alreadyReceived = grn(po, warehouse, itemA, new BigDecimal("8"));
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(itemA));
        when(goodsReceiptRepository.findByPurchaseOrderId(1L)).thenReturn(List.of(alreadyReceived));

        // 8 already received + 3 requested > 10 ordered
        GoodsReceiptRequest request = new GoodsReceiptRequest(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineRequest(10L, new BigDecimal("3"))));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, request));
    }

    @Test
    void create_rejectedGrnDoesNotCountTowardCumulativeQty() {
        Item itemA = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt rejectedGrn = grn(po, warehouse, itemA, new BigDecimal("8"));
        rejectedGrn.setStatus(DocumentStatus.REJECTED);
        rejectedGrn.setRejectReason("qty salah");
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(itemA));
        when(goodsReceiptRepository.findByPurchaseOrderId(1L)).thenReturn(List.of(rejectedGrn));
        when(goodsReceiptRepository.nextNumberValue()).thenReturn(7L);
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(invocation -> {
            GoodsReceipt saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            return saved;
        });

        GoodsReceiptRequest request = new GoodsReceiptRequest(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineRequest(10L, new BigDecimal("3"))));

        GoodsReceiptResponse response = service.create(STAFF, request);

        assertEquals("GRN-000007", response.grnNumber());
        assertEquals(DocumentStatus.PENDING, response.status());
    }

    @Test
    void approve_callsStockIncreaseForEveryLine() {
        Item itemA = item(10L, "SKU-A", "Item A");
        Item itemB = item(11L, "SKU-B", "Item B");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        po.addLine(new PurchaseOrderLine(itemB, new BigDecimal("5"), new BigDecimal("1000")));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        BigDecimal qtyA = new BigDecimal("4");
        BigDecimal qtyB = new BigDecimal("5");
        GoodsReceipt grn = new GoodsReceipt(po, warehouse, LocalDate.of(2026, 2, 1));
        grn.addLine(new GoodsReceiptLine(itemA, qtyA));
        grn.addLine(new GoodsReceiptLine(itemB, qtyB));
        grn.setGrnNumber("GRN-000005");
        grn.setCreatedBy("budi");
        ReflectionTestUtils.setField(grn, "id", 5L);
        when(goodsReceiptRepository.findById(5L)).thenReturn(Optional.of(grn));

        GoodsReceiptResponse response = service.approve(MANAGER, 5L);

        assertEquals(DocumentStatus.APPROVED, response.status());
        assertEquals("maya", response.approvedBy());
        assertNotNull(response.approvedAt());
        verify(stockService).increase(eq(itemA), eq(warehouse), eq(qtyA), eq(StockMovementRef.GRN),
                eq(5L), eq("GRN-000005"), eq("Penerimaan barang"), eq("maya"));
        verify(stockService).increase(eq(itemB), eq(warehouse), eq(qtyB), eq(StockMovementRef.GRN),
                eq(5L), eq("GRN-000005"), eq("Penerimaan barang"), eq("maya"));
    }

    @Test
    void approve_staffCannotApproveReceiptForPoAboveThreshold() {
        Item itemA = item(10L, "SKU-A", "Item A");
        // 15.000 x 1.000 = 15.000.000 -> total PO di atas threshold
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("15000"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt grn = new GoodsReceipt(po, warehouse, LocalDate.of(2026, 2, 1));
        grn.addLine(new GoodsReceiptLine(itemA, BigDecimal.ONE));
        grn.setGrnNumber("GRN-000005");
        ReflectionTestUtils.setField(grn, "id", 5L);
        when(goodsReceiptRepository.findById(5L)).thenReturn(Optional.of(grn));

        assertThrows(BusinessRuleException.class, () -> service.approve(STAFF, 5L));
        verifyNoInteractions(stockService);
    }

    @Test
    void reject_changesNoStock() {
        Item itemA = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt grn = new GoodsReceipt(po, warehouse, LocalDate.of(2026, 2, 1));
        grn.addLine(new GoodsReceiptLine(itemA, BigDecimal.ONE));
        grn.setGrnNumber("GRN-000005");
        ReflectionTestUtils.setField(grn, "id", 5L);
        when(goodsReceiptRepository.findById(5L)).thenReturn(Optional.of(grn));

        GoodsReceiptResponse response = service.reject(MANAGER, 5L, new RejectRequest("barang rusak"));

        assertEquals(DocumentStatus.REJECTED, response.status());
        assertEquals("barang rusak", response.rejectReason());
        assertEquals(DocumentStatus.REJECTED, grn.getStatus());
        // Purchase Order tetap APPROVED dan stok tidak berubah
        assertEquals(DocumentStatus.APPROVED, po.getStatus());
        verifyNoInteractions(stockService);
    }

    private static PurchaseOrder approvedPo(Item item, BigDecimal qty) {
        Supplier supplier = new Supplier();
        supplier.setCode("SUP-01");
        supplier.setName("PT Maju Jaya");
        ReflectionTestUtils.setField(supplier, "id", 1L);
        PurchaseOrder po = new PurchaseOrder(supplier, LocalDate.of(2026, 1, 10));
        po.addLine(new PurchaseOrderLine(item, qty, new BigDecimal("1000")));
        po.setStatus(DocumentStatus.APPROVED);
        po.setPoNumber("PO-000001");
        po.setCreatedBy("budi");
        ReflectionTestUtils.setField(po, "id", 1L);
        return po;
    }

    private static GoodsReceipt grn(PurchaseOrder po, Warehouse warehouse, Item item, BigDecimal qty) {
        GoodsReceipt grn = new GoodsReceipt(po, warehouse, LocalDate.of(2026, 1, 20));
        grn.addLine(new GoodsReceiptLine(item, qty));
        grn.setStatus(DocumentStatus.APPROVED);
        grn.setGrnNumber("GRN-000002");
        grn.setCreatedBy("budi");
        ReflectionTestUtils.setField(grn, "id", 2L);
        return grn;
    }

    private static Item item(Long id, String sku, String name) {
        Item item = new Item();
        item.setSku(sku);
        item.setName(name);
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }

    private static Warehouse warehouse(Long id, String code, String name) {
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(code);
        warehouse.setName(name);
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }
}
