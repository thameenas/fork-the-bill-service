package org.example.controller;

import jakarta.validation.Valid;
import org.example.model.Item;
import org.example.service.ItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/item")
@Tag(name = "Item", description = "Endpoints for managing items")
public class ItemController {
    @Autowired
    private ItemService itemService;

    @Operation(summary = "Create a new item", description = "Creates a new item with the provided details.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item created successfully")
    })
    @PostMapping
    public ResponseEntity<Item> createItem(@Valid @RequestBody Item item) {
        item.setId(UUID.randomUUID().toString());
        Item savedItem = itemService.createItem(item);
        return ResponseEntity.ok(savedItem);
    }

    @Operation(summary = "Get all items", description = "Retrieves a list of all items.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "List of items returned successfully")
    })
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        return ResponseEntity.ok(itemService.getAllItems());
    }

    @Operation(summary = "Get item by ID", description = "Retrieves an item by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item found"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable String id) {
        return itemService.getItemById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Update item", description = "Updates an existing item by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Item updated successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(@PathVariable String id, @Valid @RequestBody Item item) {
        try {
            Item updated = itemService.updateItem(id, item);
            return ResponseEntity.ok(updated);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Delete item", description = "Deletes an item by its unique identifier.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Item deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Item not found")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable String id) {
        itemService.deleteItem(id);
        return ResponseEntity.noContent().build();
    }
}
