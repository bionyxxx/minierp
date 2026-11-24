package com.minierp.sales;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
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

import com.minierp.approval.ApprovalPolicy;
import com.minierp.common.AuthenticatedUser;
import com.minierp.common.BusinessRuleException;
import com.minierp.common.DocumentStatus;
import com.minierp.common.InvalidStateException;
import com.minierp.config.MinierpProperties;
import com.minierp.customer.Customer;
import com.minierp.inventory.StockMovementRef;
import com.minierp.inventory.StockService;
import com.minierp.item.Item;
import com.minierp.item.ItemRepository;
import com.minierp.sales.dto.DeliveryLineRequest;
import com.minierp.sales.dto.DeliveryRequest;
import com.minierp.sales.dto.DeliveryResponse;
import com.minierp.sales.dto.RejectRequest;
import com.minierp.warehouse.Warehouse;
import com.minierp.warehouse.WarehouseRepository;
import com.minierp.user.Role;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceTest {

    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private SalesOrderRepository salesOrderRepository;

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private StockService stockService;

    private DeliveryService service;

    private final AuthenticatedUser admin = new AuthenticatedUser("admin1", Role.ADMIN);
    private final AuthenticatedUser staff = new AuthenticatedUser("staff1", Role.STAFF);

    @BeforeEach
    void setUp() {
        MinierpProperties properties = new MinierpProperties(
                new MinierpProperties.Security(new MinierpProperties.Jwt("secret", Duration.ofHours(8))),
                new MinierpProperties.Approval(10000000),
                new MinierpProperties.Seed(false),
                new MinierpProperties.Api("https://minierp.local/problems/"));
        service = new DeliveryService(deliveryRepository, salesOrderRepository, warehouseRepository,
                itemRepository, stockService, new ApprovalPolicy(properties));
    }

    @Test
    void createRejectsPendingSalesOrder() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.PENDING, new BigDecimal("10"));
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));

        DeliveryRequest request = new DeliveryRequest(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineRequest(5L, new BigDecimal("1"))));

        InvalidStateException ex = assertThrows(InvalidStateException.class,
                () -> service.create(admin, request));

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

        DeliveryRequest request = new DeliveryRequest(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineRequest(6L, new BigDecimal("1"))));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(admin, request));

        assertThat(ex.getMessage()).contains("tidak termasuk dalam Sales Order");
    }

    @Test
    void createRejectsWhenStockInsufficient() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(so.getLines().get(0).getItem()));
        when(deliveryRepository.findBySalesOrderId(1L)).thenReturn(List.of());
        when(stockService.availableQty(so.getLines().get(0).getItem(), warehouse))
                .thenReturn(new BigDecimal("3"));

        DeliveryRequest request = new DeliveryRequest(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineRequest(5L, new BigDecimal("5"))));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(admin, request));

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
        Delivery previous = new Delivery(so, warehouse, LocalDate.of(2026, 1, 11));
        previous.setStatus(DocumentStatus.APPROVED);
        previous.addLine(new DeliveryLine(item, new BigDecimal("8")));

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(deliveryRepository.findBySalesOrderId(1L)).thenReturn(List.of(previous));

        DeliveryRequest request = new DeliveryRequest(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineRequest(5L, new BigDecimal("5"))));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.create(admin, request));

        assertThat(ex.getMessage()).isEqualTo("Qty pengiriman untuk item SKU-1 melebihi qty pesanan");
        // Validasi stok belum sempat dijalankan
        verifyNoInteractions(stockService);
    }

    @Test
    void createIgnoresRejectedDeliveriesWhenComputingCumulativeQty() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item item = so.getLines().get(0).getItem();

        // Pengiriman REJECTED tidak mengurangi sisa qty pesanan
        Delivery rejected = new Delivery(so, warehouse, LocalDate.of(2026, 1, 11));
        rejected.setStatus(DocumentStatus.REJECTED);
        rejected.addLine(new DeliveryLine(item, new BigDecimal("8")));

        when(salesOrderRepository.findById(1L)).thenReturn(Optional.of(so));
        when(warehouseRepository.findById(1L)).thenReturn(Optional.of(warehouse));
        when(itemRepository.findById(5L)).thenReturn(Optional.of(item));
        when(deliveryRepository.findBySalesOrderId(1L)).thenReturn(List.of(rejected));
        when(stockService.availableQty(item, warehouse)).thenReturn(new BigDecimal("5"));
        when(deliveryRepository.nextNumberValue()).thenReturn(1L);
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery saved = invocation.getArgument(0);
            setId(saved, 1L);
            return saved;
        });

        DeliveryRequest request = new DeliveryRequest(1L, 1L, LocalDate.of(2026, 1, 12), null,
                List.of(new DeliveryLineRequest(5L, new BigDecimal("5"))));

        DeliveryResponse response = service.create(admin, request);

        assertThat(response.deliveryNumber()).isEqualTo("DEL-000001");
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
        when(stockService.availableQty(item, warehouse)).thenReturn(new BigDecimal("100"));
        when(deliveryRepository.nextNumberValue()).thenReturn(1L);
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery saved = invocation.getArgument(0);
            setId(saved, 1L);
            return saved;
        });

        DeliveryResponse response = service.create(admin, new DeliveryRequest(
                1L, 1L, LocalDate.of(2026, 1, 12), "kirim pagi",
                List.of(new DeliveryLineRequest(5L, new BigDecimal("4")))));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.deliveryNumber()).isEqualTo("DEL-000001");
        assertThat(response.salesOrderId()).isEqualTo(1L);
        assertThat(response.soNumber()).isEqualTo("SO-000001");
        assertThat(response.warehouseCode()).isEqualTo("G-01");
        assertThat(response.status()).isEqualTo(DocumentStatus.PENDING);
        assertThat(response.createdBy()).isEqualTo("admin1");
        assertThat(response.notes()).isEqualTo("kirim pagi");
        assertThat(response.lines()).hasSize(1);
        assertThat(response.lines().get(0).qty()).isEqualByComparingTo("4");
    }

    @Test
    void approveDecreasesStockForEveryLine() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Item itemA = newItem(5L, "SKU-1", "Widget");
        Item itemB = newItem(6L, "SKU-2", "Gadget");
        BigDecimal qtyA = new BigDecimal("2");
        BigDecimal qtyB = new BigDecimal("3");
        Delivery delivery = new Delivery(so, warehouse, LocalDate.of(2026, 1, 12));
        setId(delivery, 7L);
        delivery.setDeliveryNumber("DEL-000007");
        delivery.addLine(new DeliveryLine(itemA, qtyA));
        delivery.addLine(new DeliveryLine(itemB, qtyB));
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        DeliveryResponse response = service.approve(admin, 7L);

        assertThat(response.status()).isEqualTo(DocumentStatus.APPROVED);
        assertThat(response.approvedBy()).isEqualTo("admin1");
        assertThat(response.approvedAt()).isNotNull();
        verify(stockService).decrease(itemA, warehouse, qtyA, StockMovementRef.DELIVERY, 7L,
                "DEL-000007", "Pengiriman barang", "admin1");
        verify(stockService).decrease(itemB, warehouse, qtyB, StockMovementRef.DELIVERY, 7L,
                "DEL-000007", "Pengiriman barang", "admin1");
        verify(stockService, times(2)).decrease(any(), any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void staffCannotApproveDeliveryOfSalesOrderAboveThreshold() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10000000"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = new Delivery(so, warehouse, LocalDate.of(2026, 1, 12));
        setId(delivery, 7L);
        delivery.setDeliveryNumber("DEL-000007");
        delivery.addLine(new DeliveryLine(so.getLines().get(0).getItem(), BigDecimal.ONE));
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        assertThrows(BusinessRuleException.class, () -> service.approve(staff, 7L));

        assertThat(delivery.getStatus()).isEqualTo(DocumentStatus.PENDING);
        verifyNoInteractions(stockService);
    }

    @Test
    void approveRejectsDeliveryThatIsNotPending() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = new Delivery(so, warehouse, LocalDate.of(2026, 1, 12));
        setId(delivery, 7L);
        delivery.setDeliveryNumber("DEL-000007");
        delivery.setStatus(DocumentStatus.REJECTED);
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        assertThrows(InvalidStateException.class, () -> service.approve(admin, 7L));

        verifyNoInteractions(stockService);
    }

    @Test
    void rejectChangesNoStock() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = new Delivery(so, warehouse, LocalDate.of(2026, 1, 12));
        setId(delivery, 7L);
        delivery.setDeliveryNumber("DEL-000007");
        delivery.addLine(new DeliveryLine(so.getLines().get(0).getItem(), BigDecimal.ONE));
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        DeliveryResponse response = service.reject(admin, 7L, new RejectRequest("alamat salah"));

        assertThat(response.status()).isEqualTo(DocumentStatus.REJECTED);
        assertThat(response.rejectReason()).isEqualTo("alamat salah");
        verifyNoInteractions(stockService);
    }

    @Test
    void rejectRequiresReason() {
        SalesOrder so = newSalesOrder(1L, "SO-000001", DocumentStatus.APPROVED, new BigDecimal("10"));
        Warehouse warehouse = newWarehouse(1L, "G-01", "Gudang Utama");
        Delivery delivery = new Delivery(so, warehouse, LocalDate.of(2026, 1, 12));
        setId(delivery, 7L);
        delivery.setDeliveryNumber("DEL-000007");
        when(deliveryRepository.findById(7L)).thenReturn(Optional.of(delivery));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.reject(admin, 7L, new RejectRequest(null)));

        assertThat(ex.getMessage()).isEqualTo("reason wajib diisi");
        assertThat(delivery.getStatus()).isEqualTo(DocumentStatus.PENDING);
    }

    private SalesOrder newSalesOrder(long id, String soNumber, DocumentStatus status, BigDecimal itemPrice) {
        Customer customer = new Customer();
        customer.setCode("C-001");
        customer.setName("PT Maju");
        setId(customer, 10L);
        Item item = newItem(5L, "SKU-1", "Widget");
        SalesOrder so = new SalesOrder(customer, LocalDate.of(2026, 1, 10));
        setId(so, id);
        so.setSoNumber(soNumber);
        so.addLine(new SalesOrderLine(item, new BigDecimal("10"), itemPrice));
        so.setStatus(status);
        return so;
    }

    private static Warehouse newWarehouse(long id, String code, String name) {
        Warehouse warehouse = new Warehouse();
        warehouse.setCode(code);
        warehouse.setName(name);
        setId(warehouse, id);
        return warehouse;
    }

    private static Item newItem(long id, String sku, String name) {
        Item item = new Item();
        item.setSku(sku);
        item.setName(name);
        setId(item, id);
        return item;
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
