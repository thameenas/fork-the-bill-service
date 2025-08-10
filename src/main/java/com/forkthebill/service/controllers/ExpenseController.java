package com.forkthebill.service.controllers;

import com.forkthebill.service.models.dto.ClaimItemRequest;
import com.forkthebill.service.models.dto.ExpenseRequest;
import com.forkthebill.service.models.dto.ExpenseResponse;
import com.forkthebill.service.services.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.forkthebill.service.models.dto.PersonRequest;

@RestController
@RequestMapping("/expense")
@RequiredArgsConstructor
public class ExpenseController {
    
    private final ExpenseService expenseService;
    
    @PostMapping
    public ResponseEntity<ExpenseResponse> createExpense(@Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.createExpense(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }
    
    @GetMapping("/{slug}")
    public ResponseEntity<ExpenseResponse> getExpenseBySlug(@PathVariable String slug) {
        ExpenseResponse response = expenseService.getExpenseBySlug(slug);
        return ResponseEntity.ok(response);
    }
    
    @PutMapping("/{slug}")
    public ResponseEntity<ExpenseResponse> updateExpenseBySlug(
            @PathVariable String slug,
            @Valid @RequestBody ExpenseRequest request) {
        ExpenseResponse response = expenseService.updateExpenseBySlug(slug, request);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{slug}/items/{itemId}/claims")
    public ResponseEntity<ExpenseResponse> claimItem(
            @PathVariable String slug,
            @PathVariable String itemId,
            @Valid @RequestBody ClaimItemRequest request) {
        ExpenseResponse response = expenseService.claimItem(slug, itemId, request.getPersonId());
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{slug}/items/{itemId}/claims/{personId}")
    public ResponseEntity<ExpenseResponse> unclaimItem(
            @PathVariable String slug,
            @PathVariable String itemId,
            @PathVariable Long personId) {
        ExpenseResponse response = expenseService.unclaimItem(slug, itemId, personId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{slug}/people/{personId}/finish")
    public ResponseEntity<Void> markPersonAsFinished(
            @PathVariable String slug,
            @PathVariable Long personId) {
        expenseService.markPersonAsFinished(slug, personId);
        return ResponseEntity.ok().build();
    }
    
    @PutMapping("/{slug}/people/{personId}/pending")
    public ResponseEntity<Void> markPersonAsPending(
            @PathVariable String slug,
            @PathVariable Long personId) {
        expenseService.markPersonAsPending(slug, personId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{slug}/people")
    public ResponseEntity<ExpenseResponse> addPersonToExpense(
            @PathVariable String slug,
            @Valid @RequestBody PersonRequest personRequest) {
        ExpenseResponse response = expenseService.addPersonToExpense(slug, personRequest);
        return ResponseEntity.ok(response);
    }
}