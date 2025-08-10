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
import java.util.UUID;
import java.util.stream.Collectors;
import com.forkthebill.service.models.dto.PersonRequest;

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
    
    @Transactional
    public ExpenseResponse updateExpenseBySlug(String slug, ExpenseRequest request) {
        validateExpenseRequest(request);
        
        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        // Update basic expense properties
        expense.setPayerName(request.getPayerName());
        expense.setTotalAmount(request.getTotalAmount());
        expense.setSubtotal(request.getSubtotal());
        expense.setTax(request.getTax());
        expense.setTip(request.getTip());
        
        // Clear existing items and add new ones
        expense.getItems().clear();
        
        request.getItems().forEach(itemRequest -> {
            Item item = Item.builder()
                    .name(itemRequest.getName())
                    .price(itemRequest.getPrice())
                    .claimedBy(new ArrayList<>())
                    .build();
            expense.addItem(item);
        });
        
        Expense updatedExpense = expenseRepository.save(expense);
        
        return mapToExpenseResponse(updatedExpense);
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
                .id(person.getId())
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

    @Transactional
    public ExpenseResponse claimItem(String slug, String itemId, UUID personId) {
        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        // Validate item exists
        Item item = expense.findItemById(itemId);
        
        // Validate person exists
        Person person = expense.findPersonById(personId);
        
        // Check if already claimed
        if (item.getClaimedBy().contains(personId)) {
            throw new ValidationException("Item is already claimed by this person");
        }
        
        // Check if person has already claimed this item
        if (person.getItemsClaimed().contains(itemId)) {
            throw new ValidationException("Person has already claimed this item");
        }
        
        expense.claimItem(itemId, personId);
        
        Expense updatedExpense = expenseRepository.save(expense);
        return mapToExpenseResponse(updatedExpense);
    }

    @Transactional
    public ExpenseResponse unclaimItem(String slug, String itemId, UUID personId) {
        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        // Validate item exists
        Item item = expense.findItemById(itemId);
        
        // Validate person exists
        Person person = expense.findPersonById(personId);
        
        // Check if not claimed
        if (!item.getClaimedBy().contains(personId)) {
            throw new ValidationException("Item is not claimed by this person");
        }
        
        // Check if person hasn't claimed this item
        if (!person.getItemsClaimed().contains(itemId)) {
            throw new ValidationException("Person has not claimed this item");
        }
        
        expense.unclaimItem(itemId, personId);
        
        Expense updatedExpense = expenseRepository.save(expense);
        return mapToExpenseResponse(updatedExpense);
    }

    @Transactional
    public void markPersonAsFinished(String slug, UUID personId) {
        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        Person person = expense.findPersonById(personId);
        person.setFinished(true);
        
        expenseRepository.save(expense);
    }

    @Transactional
    public void markPersonAsPending(String slug, UUID personId) {
        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        Person person = expense.findPersonById(personId);
        person.setFinished(false);
        
        expenseRepository.save(expense);
    }

    @Transactional
    public ExpenseResponse addPersonToExpense(String slug, PersonRequest personRequest) {
        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));
        
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
        
        Expense savedExpense = expenseRepository.save(expense);
        return mapToExpenseResponse(savedExpense);
    }
}