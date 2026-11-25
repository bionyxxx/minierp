package com.minierp.application.usecase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
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

import com.minierp.domain.model.customer.Customer;
import com.minierp.domain.model.inventory.StockMovementRef;
import com.minierp.domain.model.item.Item;
import com.minierp.domain.model.sales.Delivery;
import com.minierp.domain.model.sales.DeliveryLine;
import com.minierp.domain.model.sales.SalesOrder;
import com.minierp.domain.model.sales.SalesOrderLine;
import com.minierp.domain.model.shared.AuthenticatedUser;
import com.minierp.domain.model.shared.BusinessRuleException;
import com.minierp.domain.model.shared.DocumentNumberType;
import com.minierp.domain.model.shared.DocumentStatus;
import com.minierp.domain.model.shared.InvalidStateException;
import com.minierp.domain.model.user.Role;
import com.minierp.domain.model.warehouse.Warehouse;
import com.minierp.domain.port.in.DeliveryCommand;
import com.minierp.domain.port.in.DeliveryLineCommand;
import com.minierp.domain.port.out.DeliveryRepositoryPort;
import com.minierp.domain.port.out.DocumentNumberPort;
import com.minierp.domain.port.out.ItemRepositoryPort;
import com.minierp.domain.port.out.SalesOrderRepositoryPort;
import com.minierp.domain.port.out.StockPort;
import com.minierp.domain.port.out.WarehouseRepositoryPort;
import com.minierp.domain.service.ApprovalPolicy;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepositoryPort deliveryRepository;

    @Mock
    private SalesOrderRepositoryPort salesOrderRepository;

    @Mock
    private WarehouseRepositoryPort warehouseRepository;

    @Mock
    private ItemRepositoryPort itemRepository;

    @Mock
    private StockPort stockPort;

    @Mock
    private DocumentNumberPort documentNumbers;

    private DeliveryService service;

    private final AuthenticatedUser admin = new AuthenticatedUser("admin1", Role.ADMIN);
    private final AuthenticatedUser staff = new AuthenticatedUser("staff1", Role.STAFF);

    @BeforeEach
    void setUp() {
        service = new DeliveryService(deliveryRepository, salesOrderRepository, warehouseRepository,
                itemRepository, stockPort, documentNumbers, new ApprovalPolicy(new BigDecimal("10000000")));
    }

    @Test
    void createRejectsPendingSalesOrder() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.PENDING, new BigDecimal("10"));
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));

        DeliveryCommand command = new DeliveryCommand(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineCommand(5L, new BigDecimal("1"))));

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> service.create(admin, command));

        assertThat(ex.getMessage()).isEqualTo("Delivery hanya dapat dibuat untuk Sales Order yang sudah disetujui");
    }

    @Test
    void createRejectsItemThatIsNotInSalesOrder() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item otherItem = newItem(6L, "SKU-2", "Lainnya");
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(6L)).thenReturn(Optional.of(otherItem));

        DeliveryCommand command = new DeliveryCommand(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineCommand(6L, new BigDecimal("1"))));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(admin, command));

        assertThat(ex.getMessage()).contains("tidak termasuk dalam Sales Order");
    }

    @Test
    void createRejectsWhenStockInsufficient() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item item = so.getLines().get(0).getItem();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(deliveryRepository.findBySalesOrderId(1L)).thenReturn(List.of());
        when(stockPort.availableQty(item, warehouse)).thenReturn(new BigDecimal("3"));

        DeliveryCommand command = new DeliveryCommand(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineCommand(5L, new BigDecimal("5"))));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(admin, command));

        assertThat(ex.getMessage()).contains("Stok tidak mencukupi")
                .contains("SKU-1")
                .contains("G-01")
                .contains("tersedia 3")
                .contains("diminta 5");
    }

    @Test
    void createRejectsWhenCumulativeQtyExceedsOrderedQty() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item item = so.getLines().get(0).getItem();

        // Pengiriman sebelumnya (APPROVED) sudah mengirim qty 8 dari pesanan 10
        Delivery previous = newDelivery(so, warehouse, item, new BigDecimal("8"), DocumentStatus.APPROVED);

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(deliveryRepository.findBySalesOrderId(1L)).thenReturn(List.of(previous));

        DeliveryCommand command = new DeliveryCommand(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineCommand(5L, new BigDecimal("5"))));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(admin, command));

        assertThat(ex.getMessage()).isEqualTo("Qty pengiriman untuk item SKU-1 melebihi qty pesanan");
        // Validasi stok belum sempat dijalankan
        verifyNoInteractions(stockPort);
    }

    @Test
    void createIgnoresRejectedDeliveriesWhenComputingCumulativeQty() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item item = so.getLines().get(0).getItem();

        // Pengiriman REJECTED tidak mengurangi sisa qty pesanan
        Delivery rejected = newDelivery(so, warehouse, item, new BigDecimal("8"), DocumentStatus.REJECTED);

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(deliveryRepository.findBySalesOrderId(1L)).thenReturn(List.of(rejected));
        when(stockPort.availableQty(item, warehouse)).thenReturn(new BigDecimal("5"));
        when(documentNumbers.nextNumber(DocumentNumberType.DELIVERY)).thenReturn("DEL-000001");
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery saved = invocation.getArgument(0);
            setId(saved, 1L);
            return saved;
        });

        DeliveryCommand command = new DeliveryCommand(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineCommand(5L, new BigDecimal("5"))));

        Delivery delivery = service.create(admin, command);

        assertThat(delivery.getDeliveryNumber()).isEqualTo("DEL-000001");
    }

    @Test
    void createComputesDeliveryNumberAndResponse() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item item = so.getLines().get(0).getItem();
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(deliveryRepository.findBySalesOrderId(1L)).thenReturn(List.of());
        when(stockPort.availableQty(item, warehouse)).thenReturn(new BigDecimal("100"));
        when(documentNumbers.nextNumber(DocumentNumberType.DELIVERY)).thenReturn("DEL-000001");
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery saved = invocation.getArgument(0);
            setId(saved, 1L);
            return saved;
        });

        Delivery delivery = service.create(admin, new DeliveryCommand(
                1L, 1L, LocalDate.of(2026, 1, 12), "kirim pagi",
                List.of(new DeliveryLineCommand(5L, new BigDecimal("4")))));

        assertThat(delivery.getId()).isEqualTo(1L);
        assertThat(delivery.getDeliveryNumber()).isEqualTo("DEL-000001");
        assertThat(delivery.getSalesOrder().getId()).isEqualTo(1L);
        assertThat(delivery.getSalesOrder().getSoNumber()).isEqualTo("SO-000001");
        assertThat(delivery.getWarehouse().getCode()).isEqualTo("G-01");
        assertThat(delivery.getStatus()).isEqualTo(DocumentStatus.PENDING);
        assertThat(delivery.getCreatedBy()).isEqualTo("admin1");
        assertThat(delivery.getNotes()).isEqualTo("kirim pagi");
        assertThat(delivery.getLines()).hasSize(1);
        assertThat(delivery.getLines().get(0).getQty()).isEqualByComparingTo("4");
    }

    @Test
    void approveDecreasesStockForEveryLine() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item itemA = newItem(5L, "SKU-1", "Widget");
        Item itemB = newItem(6L, "SKU-2", "Gadget");
        BigDecimal qtyA = new BigDecimal("2");
        BigDecimal qtyB = new BigDecimal("3");
        Delivery delivery = Delivery.create(so, warehouse, LocalDate.of(2026, 1, 12), null, "admin1");
        setId(delivery, 7L);
        delivery.assignNumber("DEL-000007");
        delivery.addLine(DeliveryLine.create(itemA, qtyA));
        delivery.addLine(DeliveryLine.create(itemB, qtyB));
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        Delivery approved = service.approve(admin, 7L);

        assertThat(approved.getStatus()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(approved.getApprovedBy()).isEqualTo("admin1");
        assertThat(approved.getApprovedAt()).isNotNull();
        verify(stockPort).decrease(itemA, warehouse, qtyA, StockMovementRef.DELIVERY, 7L,
                "DEL-000007", "Pengiriman barang", "admin1");
        verify(stockPort).decrease(itemB, warehouse, qtyB, StockMovementRef.DELIVERY, 7L,
                "DEL-000007", "Pengiriman barang", "admin1");
        verify(stockPort, times(2)).decrease(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void staffCannotApproveDeliveryOfSalesOrderAboveThreshold() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10000000"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = Delivery.create(so, warehouse, LocalDate.of(2026, 1, 12), null, "admin1");
        setId(delivery, 7L);
        delivery.assignNumber("DEL-000007");
        delivery.addLine(DeliveryLine.create(so.getLines().get(0).getItem(), BigDecimal.ONE));
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        assertThrows(BusinessRuleException.class, () -> service.approve(staff, 7L));

        assertThat(delivery.getStatus()).isEqualTo(DocumentStatus.PENDING);
        verifyNoInteractions(stockPort);
    }

    @Test
    void approveRejectsDeliveryThatIsNotPending() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = Delivery.create(so, warehouse, LocalDate.of(2026, 1, 12), null, "admin1");
        setId(delivery, 7L);
        delivery.assignNumber("DEL-000007");
        delivery.reject("salah");
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        assertThrows(InvalidStateException.class, () -> service.approve(admin, 7L));

        verifyNoInteractions(stockPort);
    }

    @Test
    void rejectChangesNoStock() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = Delivery.create(so, warehouse, LocalDate.of(2026, 1, 12), null, "admin1");
        setId(delivery, 7L);
        delivery.assignNumber("DEL-000007");
        delivery.addLine(DeliveryLine.create(so.getLines().get(0).getItem(), BigDecimal.ONE));
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        Delivery rejected = service.reject(admin, 7L, "alamat salah");

        assertThat(rejected.getStatus()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(rejected.getRejectReason()).isEqualTo("alamat salah");
        verifyNoInteractions(stockPort);
    }

    @Test
    void rejectRequiresReason() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = Delivery.create(so, warehouse, LocalDate.of(2026, 1, 12), null, "admin1");
        setId(delivery, 7L);
        delivery.assignNumber("DEL-000007");
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.reject(admin, 7L, null));

        assertThat(ex.getMessage()).isEqualTo("reason wajib diisi");
        assertThat(delivery.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    private SalesOrder newSalesOrder(long id, String soNumber, DocumentStatus status, BigDecimal itemPrice) {
        Item item = newItem(5L, "SKU-1", "Widget");
        SalesOrder so = SalesOrder.create(newCustomer(10L, "C-001", "PT Maju"), LocalDate.of(2026, 1, 10),
                null, "staff1");
        setId(so, id);
        so.assignNumber(soNumber);
        so.addLine(SalesOrderLine.create(item, new BigDecimal("10"), itemPrice));
        if (status == DocumentStatus.APPROVED) {
            so.approve("admin1", Instant.now());
        } else if (status == DocumentStatus.REJECTED) {
            so.reject("salah");
        }
        return so;
    }

    private static Delivery newDelivery(SalesOrder so, Warehouse warehouse, Item item, BigDecimal qty,
            DocumentStatus status) {
        Delivery delivery = Delivery.create(so, warehouse, LocalDate.of(2026, 1, 11), null, "admin1");
        delivery.assignNumber("DEL-000002");
        delivery.addLine(DeliveryLine.create(item, qty));
        if (status == DocumentStatus.APPROVED) {
            delivery.approve("admin1", Instant.now());
        } else {
            delivery.reject("salah");
        }
        setId(delivery, 2L);
        return delivery;
    }

    private static Customer newCustomer(long id, String code, String name) {
        return Customer.reconstitute(id, code, name, null, null, null, true, null, null);
    }

    private static Warehouse newWarehouse(long id, String code, String name) {
        return Warehouse.reconstitute(id, code, name, null, true, null, null);
    }

    private static Item newItem(long id, String sku, String name) {
        return Item.reconstitute(id, sku, name, "PCS", BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                true, null, null);
    }

    private static void setId(Object target, long id) {
        setField(target, "id", id);
    }

    private static void setField(Object target, String fieldName, Object value) {
        Class<?> type = target.getClass();
        while (type != null) {
            try {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                field.set(target, value);
                return;
            } catch (NoSuchFieldException ex) {
                type = type.getSuperclass();
            } catch (IllegalAccessException ex) {
                throw new IllegalStateException(ex);
            }
        }
        throw new IllegalStateException("Field %s tidak ditemukan pada %s".formatted(fieldName, target.getClass()));
    }
}
