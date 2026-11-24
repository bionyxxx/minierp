package com.minierp.item;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.minierp.common.AuthenticatedUser;
import com.minierp.common.DuplicateResourceException;
import com.minierp.item.dto.ItemRequest;
import com.minierp.item.dto.ItemResponse;
import com.minierp.user.Role;

@ExtendWith(MockitoExtension.class)
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private final AuthenticatedUser actor = new AuthenticatedUser("admin", Role.ADMIN);

    @Test
    void createWithDuplicateSkuThrowsDuplicateResourceException() {
        ItemRequest request = new ItemRequest("SKU-001", "Widget", "PCS",
                BigDecimal.ONE, BigDecimal.TEN, new BigDecimal("15.50"));
        when(itemRepository.existsBySku("SKU-001")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> itemService.create(actor, request));

        assertEquals("SKU SKU-001 sudah digunakan", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }

    @Test
    void createNormalizesNullNumericFieldsToZero() {
        ItemRequest request = new ItemRequest("SKU-001", "Widget", "PCS", null, null, null);
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.create(actor, request);

        assertEquals("SKU-001", response.sku());
        assertEquals("Widget", response.name());
        assertEquals("PCS", response.uom());
        assertEquals(0, BigDecimal.ZERO.compareTo(response.minStock()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.purchasePrice()));
        assertEquals(0, BigDecimal.ZERO.compareTo(response.sellingPrice()));
    }

    @Test
    void deleteSoftDeletesItem() {
        Item item = new Item();
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        itemService.delete(actor, 1L);

        assertFalse(item.isActive());
        verify(itemRepository).save(item);
    }

    @Test
    void updateWithUnchangedSkuSkipsUniquenessCheck() {
        Item item = new Item();
        item.setSku("SKU-001");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ItemResponse response = itemService.update(actor, 1L,
                new ItemRequest("SKU-001", "Nama baru", "PCS", null, null, null));

        assertEquals("Nama baru", response.name());
        verify(itemRepository, never()).existsBySku(anyString());
    }

    @Test
    void updateWithChangedSkuChecksUniqueness() {
        Item item = new Item();
        item.setSku("SKU-001");
        when(itemRepository.findById(1L)).thenReturn(Optional.of(item));
        when(itemRepository.existsBySku("SKU-002")).thenReturn(true);

        DuplicateResourceException exception = assertThrows(DuplicateResourceException.class,
                () -> itemService.update(actor, 1L,
                        new ItemRequest("SKU-002", "Widget", "PCS", null, null, null)));

        assertEquals("SKU SKU-002 sudah digunakan", exception.getMessage());
        verify(itemRepository, never()).save(any(Item.class));
    }
}
