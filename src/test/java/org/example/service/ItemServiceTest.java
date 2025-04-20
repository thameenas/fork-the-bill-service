package org.example.service;

import org.example.model.Item;
import org.example.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ItemServiceTest {
    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item item;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        item = new Item();
        item.setId("item-1");
        item.setName("Burger");
        item.setAmount(100.0);
        item.setAssignedTo(Collections.singletonList("participant-1"));
    }

    @Test
    void testCreateItem() {
        when(itemRepository.save(any(Item.class))).thenReturn(item);
        Item created = itemService.createItem(item);
        assertEquals(item, created);
    }

    @Test
    void testGetAllItems() {
        when(itemRepository.findAll()).thenReturn(Collections.singletonList(item));
        List<Item> items = itemService.getAllItems();
        assertEquals(1, items.size());
        assertEquals(item, items.getFirst());
    }

    @Test
    void testGetItemById() {
        when(itemRepository.findById(eq("item-1"))).thenReturn(Optional.of(item));
        Optional<Item> found = itemService.getItemById("item-1");
        assertTrue(found.isPresent());
        assertEquals(item, found.get());
    }

    @Test
    void testUpdateItem() {
        Item updated = new Item();
        updated.setId("item-1");
        updated.setName("Pizza");
        updated.setAmount(200.0);
        updated.setAssignedTo(Collections.singletonList("participant-2"));
        when(itemRepository.findById(eq("item-1"))).thenReturn(Optional.of(item));
        when(itemRepository.save(any(Item.class))).thenReturn(updated);
        Item result = itemService.updateItem("item-1", updated);
        assertEquals("Pizza", result.getName());
        assertEquals(200.0, result.getAmount());
        assertEquals(Collections.singletonList("participant-2"), result.getAssignedTo());
    }

    @Test
    void testDeleteItem() {
        doNothing().when(itemRepository).deleteById(eq("item-1"));
        itemService.deleteItem("item-1");
        verify(itemRepository, times(1)).deleteById("item-1");
    }
}
