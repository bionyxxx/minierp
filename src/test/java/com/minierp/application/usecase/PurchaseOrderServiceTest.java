package com.minierp.application.usecase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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

import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.purchase.PurchaseOrder;
import com.minierp.domain.model.purchase.PurchaseOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.InvalidStateException;
import com.minierp.domain.model.shared.NotFoundException;
import com.minierp.domain.model.supplier.Supplier;
import com.minierp.domain.model.user.Role;
import com.minierp.domain.port.in.PurchaseOrderCommand;
import com.minierp.domain.port.in.PurchaseOrderLineCommand;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.PurchaseOrderRepositoryPort;
import com.minierp.domain.port.out.SupplierRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    private static final AuthenticatedUser STAFF = new AuthenticatedUser("budi", Role.STAFF);
    private static final AuthenticatedUser MANAGER = new AuthenticatedUser("maya", Role.MANAGER);

    @Mock
    private PurchaseOrderRepositoryPort purchaseOrderRepository;

    @Mock
    private SupplierRepositoryPort supplierRepository;

    @Mock
    private ItemRepositoryPort itemRepository;

    @Mock
    private DocumentNumberPort documentNumbers;

    private PurchaseOrderService service;

    @BeforeEach
    void setUp() {
        service = new PurchaseOrderService(purchaseOrderRepository, supplierRepository, itemRepository,
                documentNumbers, new ApprovalPolicy(new BigDecimal("10000000")));
    }

    @Test
    void create_computesTotalAmountAndDocumentNumber() {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        Item itemA = item(10L, "SKU-A", "Item A");
        Item itemB = item(11L, "SKU-B", "Item B");
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(itemA));
        when(itemRepository.findById(11L)).thenReturn(Optional.of(itemB));
        when(documentNumbers.nextNumber(DocumentNumberType.PURCHASE_ORDER)).thenReturn("PO-000001");
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        PurchaseOrderCommand command = new PurchaseOrderCommand(1L, LocalDate.of(2026, 1, 15), "catatan",
                List.of(new PurchaseOrderLineCommand(10L, new BigDecimal("2"), new BigDecimal("1500.50")),
                        new PurchaseOrderLineCommand(11L, new BigDecimal("3"), new BigDecimal("1000"))));

        PurchaseOrder po = service.create(STAFF, command);

        assertEquals("PO-000001", po.getPoNumber());
        assertEquals(0, new BigDecimal("6001.00").compareTo(po.getTotalAmount()));
        assertEquals(DocumentStatus.PENDING, po.getStatus());
        assertEquals("budi", po.getCreatedBy());
        assertEquals(1L, po.getSupplier().getId().longValue());
        assertEquals("PT Maju Jaya", po.getSupplier().getName());
        assertEquals(2, po.getLines().size());
        assertEquals(0, new BigDecimal("3001.00").compareTo(po.getLines().get(0).getLineTotal()));
        assertEquals("SKU-A", po.getLines().get(0).getItem().getSku());
    }

    @Test
    void create_emptyLinesThrowsBusinessRuleException() {
        PurchaseOrderCommand command = new PurchaseOrderCommand(1L, LocalDate.of(2026, 1, 15), null, List.of());

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, command));
    }

    @Test
    void create_inactiveSupplierThrowsBusinessRuleException() {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        supplier.deactivate();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        PurchaseOrderCommand command = new PurchaseOrderCommand(1L, LocalDate.of(2026, 1, 15), null,
                List.of(new PurchaseOrderLineCommand(10L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, command));
    }

    @Test
    void create_inactiveItemThrowsBusinessRuleException() {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        Item item = item(10L, "SKU-A", "Item A");
        item.deactivate();
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        PurchaseOrderCommand command = new PurchaseOrderCommand(1L, LocalDate.of(2026, 1, 15), null,
                List.of(new PurchaseOrderLineCommand(10L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, command));
    }

    @Test
    void create_unknownSupplierThrowsNotFoundException() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        PurchaseOrderCommand command = new PurchaseOrderCommand(99L, LocalDate.of(2026, 1, 15), null,
                List.of(new PurchaseOrderLineCommand(10L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(NotFoundException.class, () -> service.create(STAFF, command));
    }

    @Test
    void approve_staffCannotApprovePoAtThreshold() {
        PurchaseOrder po = pendingPoWithTotal(new BigDecimal("10000000"));
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        assertThrows(BusinessRuleException.class, () -> service.approve(STAFF, 9L));
        assertEquals(DocumentStatus.PENDING, po.getStatus());
        verify(purchaseOrderRepository, never()).save(any(PurchaseOrder.class));
    }

    @Test
    void approve_managerCanApprovePoAboveThreshold() {
        PurchaseOrder po = pendingPoWithTotal(new BigDecimal("15000000"));
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        PurchaseOrder approved = service.approve(MANAGER, 9L);

        assertEquals(DocumentStatus.APPROVED, approved.getStatus());
        assertEquals("maya", approved.getApprovedBy());
        assertNotNull(approved.getApprovedAt());
        assertEquals(DocumentStatus.APPROVED, po.getStatus());
    }

    @Test
    void approve_alreadyProcessedPoThrowsInvalidStateException() {
        PurchaseOrder po = pendingPoWithTotal(BigDecimal.TEN);
        po.approve("maya", Instant.now());
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        assertThrows(InvalidStateException.class, () -> service.approve(STAFF, 9L));
    }

    @Test
    void reject_staffCannotRejectPoAboveThreshold() {
        PurchaseOrder po = pendingPoWithTotal(new BigDecimal("15000000"));
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        assertThrows(BusinessRuleException.class, () -> service.reject(STAFF, 9L, "mahal"));
        assertEquals(DocumentStatus.PENDING, po.getStatus());
    }

    @Test
    void reject_setsStatusAndRejectReason() {
        PurchaseOrder po = pendingPoWithTotal(BigDecimal.TEN);
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        PurchaseOrder rejected = service.reject(STAFF, 9L, "harga tidak sesuai");

        assertEquals(DocumentStatus.REJECTED, rejected.getStatus());
        assertEquals("harga tidak sesuai", rejected.getRejectReason());
        assertEquals(DocumentStatus.REJECTED, po.getStatus());
        assertEquals("harga tidak sesuai", po.getRejectReason());
    }

    private PurchaseOrder pendingPoWithTotal(BigDecimal totalAmount) {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        Item item = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = PurchaseOrder.create(supplier, LocalDate.of(2026, 1, 10), null, "budi");
        po.addLine(PurchaseOrderLine.create(item, BigDecimal.ONE, totalAmount));
        po.assignNumber("PO-000009");
        ReflectionTestUtils.setField(po, "id", 9L);
        return po;
    }

    private static Supplier supplier(Long id, String code, String name) {
        return Supplier.reconstitute(id, code, name, null, null, null, true, null, null);
    }

    private static Item item(Long id, String sku, String name) {
        return Item.reconstitute(id, sku, name, "PCS", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                true, null, null);
    }
}
