package com.minierp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.purchase.GoodsReceipt;
import com.minierp.domain.model.purchase.GoodsReceiptLine;
import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.purchase.PurchaseOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.InvalidStateException;
import com.minierp.domain.model.supplier.Supplier;
import com.minierp.domain.model.user.Role;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.in.GoodsReceiptCommand;
import com.minierp.domain.port.in.GoodsReceiptLineCommand;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.GoodsReceiptRepositoryPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.PurchaseOrderRepositoryPort;
import com.minierp.domain.port.out.StockPort;
import com.minierp.domain.port.out.WarehouseRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

@ExtendWith(MockitoExtension.class)
class GoodsReceiptServiceTest {

    private static final AuthenticatedUser STAFF = new AuthenticatedUser("budi", Role.STAFF);
    private static final AuthenticatedUser MANAGER = new AuthenticatedUser("maya", Role.MANAGER);

    @Mock
    private GoodsReceiptRepositoryPort goodsReceiptRepository;

    @Mock
    private PurchaseOrderRepositoryPort purchaseOrderRepository;

    @Mock
    private WarehouseRepositoryPort warehouseRepository;

    @Mock
    private ItemRepositoryPort itemRepository;

    @Mock
    private StockPort stockPort;

    @Mock
    private DocumentNumberPort documentNumbers;

    private GoodsReceiptService service;

    @BeforeEach
    void setUp() {
        service = new GoodsReceiptService(goodsReceiptRepository, purchaseOrderRepository, warehouseRepository,
                itemRepository, stockPort, documentNumbers, new ApprovalPolicy(new BigDecimal("10000000")));
    }

    @Test
    void create_onPendingPoThrowsInvalidStateException() {
        PurchaseOrder po = pendingPo(item(10L, "SKU-A", "Item A"), new BigDecimal("10"));
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));

        GoodsReceiptCommand command = new GoodsReceiptCommand(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineCommand(10L, BigDecimal.ONE)));

        assertThrows(InvalidStateException.class, () -> service.create(STAFF, command));
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
        when(documentNumbers.nextNumber(DocumentNumberType.GOODS_RECEIPT)).thenReturn("GRN-000001");
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(invocation -> {
            GoodsReceipt saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        GoodsReceiptCommand command = new GoodsReceiptCommand(1L, 2L, LocalDate.of(2026, 2, 1), "diterima lengkap",
                List.of(new GoodsReceiptLineCommand(10L, new BigDecimal("4"))));

        GoodsReceipt grn = service.create(STAFF, command);

        assertEquals("GRN-000001", grn.getGrnNumber());
        assertEquals("PO-000001", grn.getPurchaseOrder().getPoNumber());
        assertEquals(2L, grn.getWarehouse().getId().longValue());
        assertEquals("WH-01", grn.getWarehouse().getCode());
        assertEquals(DocumentStatus.PENDING, grn.getStatus());
        assertEquals("budi", grn.getCreatedBy());
        assertEquals(1, grn.getLines().size());
        assertEquals(0, new BigDecimal("4").compareTo(grn.getLines().get(0).getQty()));
        assertEquals("SKU-A", grn.getLines().get(0).getItem().getSku());
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

        GoodsReceiptCommand command = new GoodsReceiptCommand(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineCommand(11L, BigDecimal.ONE)));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, command));
    }

    @Test
    void create_cumulativeQtyOverflowThrowsBusinessRuleException() {
        Item itemA = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt alreadyReceived = grn(po, warehouse, itemA, new BigDecimal("8"), DocumentStatus.APPROVED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(itemA));
        when(goodsReceiptRepository.findByPurchaseOrderId(1L)).thenReturn(List.of(alreadyReceived));

        // 8 already received + 3 requested > 10 ordered
        GoodsReceiptCommand command = new GoodsReceiptCommand(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineCommand(10L, new BigDecimal("3"))));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, command));
    }

    @Test
    void create_rejectedGrnDoesNotCountTowardCumulativeQty() {
        Item itemA = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt rejectedGrn = grn(po, warehouse, itemA, new BigDecimal("8"), DocumentStatus.REJECTED);
        when(purchaseOrderRepository.findById(1L)).thenReturn(Optional.of(po));
        when(warehouseRepository.findById(2L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(itemA));
        when(goodsReceiptRepository.findByPurchaseOrderId(1L)).thenReturn(List.of(rejectedGrn));
        when(documentNumbers.nextNumber(DocumentNumberType.GOODS_RECEIPT)).thenReturn("GRN-000007");
        when(goodsReceiptRepository.save(any(GoodsReceipt.class))).thenAnswer(invocation -> {
            GoodsReceipt saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 7L);
            return saved;
        });

        GoodsReceiptCommand command = new GoodsReceiptCommand(1L, 2L, LocalDate.of(2026, 2, 1), null,
                List.of(new GoodsReceiptLineCommand(10L, new BigDecimal("3"))));

        GoodsReceipt grn = service.create(STAFF, command);

        assertEquals("GRN-000007", grn.getGrnNumber());
        assertEquals(DocumentStatus.PENDING, grn.getStatus());
    }

    @Test
    void approve_callsStockIncreaseForEveryLine() {
        Item itemA = item(10L, "SKU-A", "Item A");
        Item itemB = item(11L, "SKU-B", "Item B");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        po.addLine(PurchaseOrderLine.create(itemB, new BigDecimal("5"), new BigDecimal("1000")));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        BigDecimal qtyA = new BigDecimal("4");
        BigDecimal qtyB = new BigDecimal("5");
        GoodsReceipt grn = GoodsReceipt.create(po, warehouse, LocalDate.of(2026, 2, 1), null, "budi");
        grn.addLine(GoodsReceiptLine.create(itemA, qtyA));
        grn.addLine(GoodsReceiptLine.create(itemB, qtyB));
        grn.assignNumber("GRN-000005");
        ReflectionTestUtils.setField(grn, "id", 5L);
        when(goodsReceiptRepository.findById(5L)).thenReturn(Optional.of(grn));

        GoodsReceipt approved = service.approve(MANAGER, 5L);

        assertEquals(DocumentStatus.APPROVED, approved.getStatus());
        assertEquals("maya", approved.getApprovedBy());
        assertNotNull(approved.getApprovedAt());
        verify(stockPort).increase(eq(itemA), eq(warehouse), eq(qtyA), eq(StockMovementRef.GRN),
                eq(5L), eq("GRN-000005"), eq("Penerimaan barang"), eq("maya"));
        verify(stockPort).increase(eq(itemB), eq(warehouse), eq(qtyB), eq(StockMovementRef.GRN),
                eq(5L), eq("GRN-000005"), eq("Penerimaan barang"), eq("maya"));
    }

    @Test
    void approve_staffCannotApproveReceiptForPoAboveThreshold() {
        Item itemA = item(10L, "SKU-A", "Item A");
        // 15.000 x 1.000 = 15.000.000 -> total PO di atas threshold
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("15000"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt grn = GoodsReceipt.create(po, warehouse, LocalDate.of(2026, 2, 1), null, "budi");
        grn.addLine(GoodsReceiptLine.create(itemA, BigDecimal.ONE));
        grn.assignNumber("GRN-000005");
        ReflectionTestUtils.setField(grn, "id", 5L);
        when(goodsReceiptRepository.findById(5L)).thenReturn(Optional.of(grn));

        assertThrows(BusinessRuleException.class, () -> service.approve(STAFF, 5L));
        verifyNoInteractions(stockPort);
    }

    @Test
    void reject_changesNoStock() {
        Item itemA = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = approvedPo(itemA, new BigDecimal("10"));
        Warehouse warehouse = warehouse(2L, "WH-01", "Gudang Utama");
        GoodsReceipt grn = GoodsReceipt.create(po, warehouse, LocalDate.of(2026, 2, 1), null, "budi");
        grn.addLine(GoodsReceiptLine.create(itemA, BigDecimal.ONE));
        grn.assignNumber("GRN-000005");
        ReflectionTestUtils.setField(grn, "id", 5L);
        when(goodsReceiptRepository.findById(5L)).thenReturn(Optional.of(grn));

        GoodsReceipt rejected = service.reject(MANAGER, 5L, "barang rusak");

        assertEquals(DocumentStatus.REJECTED, rejected.getStatus());
        assertEquals("barang rusak", rejected.getRejectReason());
        assertEquals(DocumentStatus.REJECTED, grn.getStatus());
        // Purchase Order tetap APPROVED dan stok tidak berubah
        assertEquals(DocumentStatus.APPROVED, po.getStatus());
        verifyNoInteractions(stockPort);
    }

    private static PurchaseOrder pendingPo(Item item, BigDecimal qty) {
        Supplier supplier = Supplier.reconstitute(1L, "SUP-01", "PT Maju Jaya", null, null, null, true, null, null);
        PurchaseOrder po = PurchaseOrder.create(supplier, LocalDate.of(2026, 1, 10), null, "budi");
        po.addLine(PurchaseOrderLine.create(item, qty, new BigDecimal("1000")));
        po.assignNumber("PO-000001");
        ReflectionTestUtils.setField(po, "id", 1L);
        return po;
    }

    private static PurchaseOrder approvedPo(Item item, BigDecimal qty) {
        PurchaseOrder po = pendingPo(item, qty);
        po.approve("maya", Instant.now());
        return po;
    }

    private static GoodsReceipt grn(PurchaseOrder po, Warehouse warehouse, Item item, BigDecimal qty,
            DocumentStatus status) {
        GoodsReceipt grn = GoodsReceipt.create(po, warehouse, LocalDate.of(2026, 1, 20), null, "budi");
        grn.addLine(GoodsReceiptLine.create(item, qty));
        grn.assignNumber("GRN-000002");
        if (status == DocumentStatus.APPROVED) {
            grn.approve("maya", Instant.now());
        } else {
            grn.reject("qty salah");
        }
        ReflectionTestUtils.setField(grn, "id", 2L);
        return grn;
    }

    private static Item item(Long id, String sku, String name) {
        return Item.reconstitute(id, sku, name, "PCS", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                true, null, null);
    }

    private static Warehouse warehouse(Long id, String code, String name) {
        return Warehouse.reconstitute(id, code, name, null, true, null, null);
    }
}
