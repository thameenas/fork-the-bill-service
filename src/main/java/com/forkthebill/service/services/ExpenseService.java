package com.forkthebill.service.services;

import com.forkthebill.service.exceptions.ResourceNotFoundException;
import com.forkthebill.service.exceptions.ValidationException;
import com.forkthebill.service.models.dto.ExpenseRequest;
import com.forkthebill.service.models.dto.ExpenseResponse;
import com.forkthebill.service.models.dto.ItemResponse;
import com.forkthebill.service.models.dto.PersonResponse;
import com.forkthebill.service.models.entities.Expense;
import com.forkthebill.service.models.entities.Item;
import com.forkthebill.service.models.entities.Person;
import com.forkthebill.service.repositories.ExpenseRepository;
import com.forkthebill.service.utils.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    
    private final ExpenseRepository expenseRepository;
    private final SlugGenerator slugGenerator;
    
    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
        validateExpenseRequest(request);
        
        String slug = slugGenerator.generateUniqueSlug();
        
        Expense expense = Expense.builder()
                .payerName(request.getPayerName())
                .totalAmount(request.getTotalAmount())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .tip(request.getTip())
                .createdAt(LocalDateTime.now())
                .slug(slug)
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();
        
        // Add items
        request.getItems().forEach(itemRequest -> {
            Item item = Item.builder()
                    .name(itemRequest.getName())
                    .price(itemRequest.getPrice())
                    .claimedBy(new ArrayList<>())
                    .build();
            expense.addItem(item);
        });
        
        // Add people if provided
        if (request.getPeople() != null && !request.getPeople().isEmpty()) {
            request.getPeople().forEach(personRequest -> {
                Person person = Person.builder()
                        .name(personRequest.getName())
                        .amountOwed(personRequest.getAmountOwed() != null ? personRequest.getAmountOwed() : BigDecimal.ZERO)
                        .subtotal(personRequest.getSubtotal() != null ? personRequest.getSubtotal() : BigDecimal.ZERO)
                        .taxShare(personRequest.getTaxShare() != null ? personRequest.getTaxShare() : BigDecimal.ZERO)
                        .tipShare(personRequest.getTipShare() != null ? personRequest.getTipShare() : BigDecimal.ZERO)
                        .totalOwed(personRequest.getTotalOwed() != null ? personRequest.getTotalOwed() : BigDecimal.ZERO)
                        .isFinished(personRequest.isFinished())
                        .itemsClaimed(new ArrayList<>())
                        .build();
                expense.addPerson(person);
            });
        }
        
        Expense savedExpense = expenseRepository.save(expense);
        
        return mapToExpenseResponse(savedExpense);
    }
    
    public ExpenseResponse getExpenseBySlug(String slug) {
        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        return mapToExpenseResponse(expense);
    }
    
    private void validateExpenseRequest(ExpenseRequest request) {
        BigDecimal calculatedTotal = request.getSubtotal().add(request.getTax()).add(request.getTip());
        
        if (request.getTotalAmount().compareTo(calculatedTotal) != 0) {
            throw new ValidationException("Total amount must equal subtotal + tax + tip");
        }
    }
    
    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .slug(expense.getSlug())
                .createdAt(expense.getCreatedAt())
                .payerName(expense.getPayerName())
                .totalAmount(expense.getTotalAmount())
                .subtotal(expense.getSubtotal())
                .tax(expense.getTax())
                .tip(expense.getTip())
                .items(expense.getItems().stream()
                        .map(this::mapToItemResponse)
                        .collect(Collectors.toList()))
                .people(expense.getPeople().stream()
                        .map(this::mapToPersonResponse)
                        .collect(Collectors.toList()))
                .build();
    }
    
    private ItemResponse mapToItemResponse(Item item) {
        return ItemResponse.builder()
                .id(item.getId())
                .name(item.getName())
                .price(item.getPrice())
                .claimedBy(item.getClaimedBy())
                .build();
    }
    
    private PersonResponse mapToPersonResponse(Person person) {
        return PersonResponse.builder()
                .name(person.getName())
                .itemsClaimed(person.getItemsClaimed())
                .amountOwed(person.getAmountOwed())
                .subtotal(person.getSubtotal())
                .taxShare(person.getTaxShare())
                .tipShare(person.getTipShare())
                .totalOwed(person.getTotalOwed())
                .isFinished(person.isFinished())
                .build();
    }
}