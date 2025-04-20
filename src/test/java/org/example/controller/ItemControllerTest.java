package org.example.controller;

import org.example.model.Item;
import org.example.service.ItemService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class ItemControllerTest {
    @Mock
    private ItemService itemService;

    @InjectMocks
    private ItemController itemController;

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
        when(itemService.createItem(any(Item.class))).thenReturn(item);
        ResponseEntity<Item> response = itemController.createItem(item);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Burger", response.getBody().getName());
    }

    @Test
    void testGetAllItems() {
        when(itemService.getAllItems()).thenReturn(Collections.singletonList(item));
        ResponseEntity<List<Item>> response = itemController.getAllItems();
        assertEquals(200, response.getStatusCode().value());
        assertEquals(1, response.getBody().size());
    }

    @Test
    void testGetItemById() {
        when(itemService.getItemById(eq("item-1"))).thenReturn(Optional.of(item));
        ResponseEntity<Item> response = itemController.getItem("item-1");
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Burger", response.getBody().getName());
    }

    @Test
    void testGetItemByIdNotFound() {
        when(itemService.getItemById(eq("notfound"))).thenReturn(Optional.empty());
        ResponseEntity<Item> response = itemController.getItem("notfound");
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testUpdateItem() {
        Item updated = new Item();
        updated.setId("item-1");
        updated.setName("Pizza");
        updated.setAmount(200.0);
        updated.setAssignedTo(Collections.singletonList("participant-2"));
        when(itemService.updateItem(eq("item-1"), any(Item.class))).thenReturn(updated);
        ResponseEntity<Item> response = itemController.updateItem("item-1", updated);
        assertEquals(200, response.getStatusCode().value());
        assertEquals("Pizza", response.getBody().getName());
    }

    @Test
    void testUpdateItemNotFound() {
        when(itemService.updateItem(eq("notfound"), any(Item.class))).thenThrow(new RuntimeException("Item not found"));
        ResponseEntity<Item> response = itemController.updateItem("notfound", item);
        assertEquals(404, response.getStatusCode().value());
    }

    @Test
    void testDeleteItem() {
        doNothing().when(itemService).deleteItem(eq("item-1"));
        ResponseEntity<Void> response = itemController.deleteItem("item-1");
        assertEquals(204, response.getStatusCode().value());
    }
}
