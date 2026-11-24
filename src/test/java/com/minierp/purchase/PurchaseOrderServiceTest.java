package com.minierp.purchase;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
import com.minierp.common.NotFoundException;
import com.minierp.config.MinierpProperties;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.purchase.dto.PurchaseOrderLineRequest;
import com.minierp.purchase.dto.PurchaseOrderRequest;
import com.minierp.purchase.dto.PurchaseOrderResponse;
import com.minierp.purchase.dto.RejectRequest;
import com.minierp.supplier.Supplier;
import com.minierp.supplier.SupplierRepository;
import com.minierp.user.Role;

@ExtendWith(MockitoExtension.class)
class PurchaseOrderServiceTest {

    private static final AuthenticatedUser STAFF = new AuthenticatedUser("budi", Role.STAFF);
    private static final AuthenticatedUser MANAGER = new AuthenticatedUser("maya", Role.MANAGER);

    @Mock
    private PurchaseOrderRepository purchaseOrderRepository;

    @Mock
    private SupplierRepository supplierRepository;

    @Mock
    private ItemRepository itemRepository;

    private PurchaseOrderService service;

    @BeforeEach
    void setUp() {
        ApprovalPolicy approvalPolicy = new ApprovalPolicy(new MinierpProperties(
                new MinierpProperties.Security(new MinierpProperties.Jwt("secret", Duration.ofHours(8))),
                new MinierpProperties.Approval(10000000),
                new MinierpProperties.Seed(false),
                new MinierpProperties.Api("https://minierp.local/problems/")));
        service = new PurchaseOrderService(purchaseOrderRepository, supplierRepository, itemRepository,
                approvalPolicy);
    }

    @Test
    void create_computesTotalAmountAndDocumentNumber() {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        Item itemA = item(10L, "SKU-A", "Item A");
        Item itemB = item(11L, "SKU-B", "Item B");
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(itemA));
        when(itemRepository.findById(11L)).thenReturn(Optional.of(itemB));
        when(purchaseOrderRepository.nextNumberValue()).thenReturn(1L);
        when(purchaseOrderRepository.save(any(PurchaseOrder.class))).thenAnswer(invocation -> {
            PurchaseOrder saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        PurchaseOrderRequest request = new PurchaseOrderRequest(1L, LocalDate.of(2026, 1, 15), "catatan",
                List.of(new PurchaseOrderLineRequest(10L, new BigDecimal("2"), new BigDecimal("1500.50")),
                        new PurchaseOrderLineRequest(11L, new BigDecimal("3"), new BigDecimal("1000"))));

        PurchaseOrderResponse response = service.create(STAFF, request);

        assertEquals("PO-000001", response.poNumber());
        assertEquals(0, new BigDecimal("6001.00").compareTo(response.totalAmount()));
        assertEquals(DocumentStatus.PENDING, response.status());
        assertEquals("budi", response.createdBy());
        assertEquals(1L, response.supplierId().longValue());
        assertEquals("PT Maju Jaya", response.supplierName());
        assertEquals(2, response.lines().size());
        assertEquals(0, new BigDecimal("3001.00").compareTo(response.lines().get(0).lineTotal()));
        assertEquals("SKU-A", response.lines().get(0).sku());
    }

    @Test
    void create_emptyLinesThrowsBusinessRuleException() {
        PurchaseOrderRequest request = new PurchaseOrderRequest(1L, LocalDate.of(2026, 1, 15), null, List.of());

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, request));
    }

    @Test
    void create_inactiveSupplierThrowsBusinessRuleException() {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        supplier.setActive(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));

        PurchaseOrderRequest request = new PurchaseOrderRequest(1L, LocalDate.of(2026, 1, 15), null,
                List.of(new PurchaseOrderLineRequest(10L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, request));
    }

    @Test
    void create_inactiveItemThrowsBusinessRuleException() {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        Item item = item(10L, "SKU-A", "Item A");
        item.setActive(false);
        when(supplierRepository.findById(1L)).thenReturn(Optional.of(supplier));
        when(itemRepository.findById(10L)).thenReturn(Optional.of(item));

        PurchaseOrderRequest request = new PurchaseOrderRequest(1L, LocalDate.of(2026, 1, 15), null,
                List.of(new PurchaseOrderLineRequest(10L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(BusinessRuleException.class, () -> service.create(STAFF, request));
    }

    @Test
    void create_unknownSupplierThrowsNotFoundException() {
        when(supplierRepository.findById(99L)).thenReturn(Optional.empty());

        PurchaseOrderRequest request = new PurchaseOrderRequest(99L, LocalDate.of(2026, 1, 15), null,
                List.of(new PurchaseOrderLineRequest(10L, BigDecimal.ONE, BigDecimal.TEN)));

        assertThrows(NotFoundException.class, () -> service.create(STAFF, request));
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

        PurchaseOrderResponse response = service.approve(MANAGER, 9L);

        assertEquals(DocumentStatus.APPROVED, response.status());
        assertEquals("maya", response.approvedBy());
        assertNotNull(response.approvedAt());
        assertEquals(DocumentStatus.APPROVED, po.getStatus());
    }

    @Test
    void approve_alreadyProcessedPoThrowsInvalidStateException() {
        PurchaseOrder po = pendingPoWithTotal(BigDecimal.TEN);
        po.setStatus(DocumentStatus.APPROVED);
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        assertThrows(InvalidStateException.class, () -> service.approve(STAFF, 9L));
    }

    @Test
    void reject_staffCannotRejectPoAboveThreshold() {
        PurchaseOrder po = pendingPoWithTotal(new BigDecimal("15000000"));
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        assertThrows(BusinessRuleException.class, () -> service.reject(STAFF, 9L, new RejectRequest("mahal")));
        assertEquals(DocumentStatus.PENDING, po.getStatus());
    }

    @Test
    void reject_setsStatusAndRejectReason() {
        PurchaseOrder po = pendingPoWithTotal(BigDecimal.TEN);
        when(purchaseOrderRepository.findById(9L)).thenReturn(Optional.of(po));

        PurchaseOrderResponse response = service.reject(STAFF, 9L, new RejectRequest("harga tidak sesuai"));

        assertEquals(DocumentStatus.REJECTED, response.status());
        assertEquals("harga tidak sesuai", response.rejectReason());
        assertEquals(DocumentStatus.REJECTED, po.getStatus());
        assertEquals("harga tidak sesuai", po.getRejectReason());
    }

    private PurchaseOrder pendingPoWithTotal(BigDecimal totalAmount) {
        Supplier supplier = supplier(1L, "SUP-01", "PT Maju Jaya");
        Item item = item(10L, "SKU-A", "Item A");
        PurchaseOrder po = new PurchaseOrder(supplier, LocalDate.of(2026, 1, 10));
        po.addLine(new PurchaseOrderLine(item, BigDecimal.ONE, totalAmount));
        po.setPoNumber("PO-000009");
        po.setCreatedBy("budi");
        ReflectionTestUtils.setField(po, "id", 9L);
        return po;
    }

    private static Supplier supplier(Long id, String code, String name) {
        Supplier supplier = new Supplier();
        supplier.setCode(code);
        supplier.setName(name);
        ReflectionTestUtils.setField(supplier, "id", id);
        return supplier;
    }

    private static Item item(Long id, String sku, String name) {
        Item item = new Item();
        item.setSku(sku);
        item.setName(name);
        ReflectionTestUtils.setField(item, "id", id);
        return item;
    }
}
