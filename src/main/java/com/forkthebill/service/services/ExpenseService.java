package com.forkthebill.service.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkthebill.service.exceptions.ResourceNotFoundException;
import com.forkthebill.service.exceptions.ValidationException;
import com.forkthebill.service.models.dto.*;
import com.forkthebill.service.models.entities.Expense;
import com.forkthebill.service.models.entities.Item;
import com.forkthebill.service.models.entities.Person;
import com.forkthebill.service.repositories.ExpenseRepository;
import com.forkthebill.service.utils.SlugGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final SlugGenerator slugGenerator;
    private final GeminiService geminiService;

    @Transactional
    public ExpenseResponse createExpense(ExpenseRequest request) {
//        validateExpenseRequest(request);

        String slug = slugGenerator.generateUniqueSlug();

        Expense expense = Expense.builder()
                .payerName(request.getPayerName())
                .restaurantName(request.getRestaurantName())
                .totalAmount(request.getTotalAmount())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .serviceCharge(request.getServiceCharge())
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
                    .quantity(itemRequest.getQuantity())
                    .totalQuantity(itemRequest.getTotalQuantity())
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
                        .serviceChargeShare(personRequest.getServiceChargeShare() != null ? personRequest.getServiceChargeShare() : BigDecimal.ZERO)
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
//        validateExpenseRequest(request);

        Expense expense = expenseRepository.findBySlug(slug)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found with slug: " + slug));

        expense.setPayerName(request.getPayerName());
        expense.setTax(request.getTax());
        expense.setServiceCharge(request.getServiceCharge());
        expense.setDiscount(request.getDiscount());

        request.getItems().forEach(itemRequest -> {
            Optional<Item> matchingExisting = (itemRequest.getId() == null)
                    ? Optional.empty()
                    : expense.getItems().stream()
                            .filter(existing -> Objects.equals(existing.getId(), itemRequest.getId()))
                            .findFirst();

            if (matchingExisting.isPresent()) {
                Item existing = matchingExisting.get();
                existing.setName(itemRequest.getName());
                existing.setPrice(itemRequest.getPrice());
                existing.setQuantity(itemRequest.getQuantity());
                existing.setTotalQuantity(itemRequest.getTotalQuantity());
            } else {
                Item item = Item.builder()
                        .name(itemRequest.getName())
                        .price(itemRequest.getPrice())
                        .quantity(itemRequest.getQuantity())
                        .totalQuantity(itemRequest.getTotalQuantity())
                        .claimedBy(new ArrayList<>())
                        .build();
                expense.addItem(item);
            }
        });
        expense.recalculateAmounts();
        Expense updatedExpense = expenseRepository.save(expense);

        return mapToExpenseResponse(updatedExpense);
    }

    private void validateExpenseRequest(ExpenseRequest request) {
        BigDecimal calculatedTotal = request.getSubtotal().add(request.getTax()).add(request.getServiceCharge());
        BigDecimal margin = new BigDecimal("5.00"); // 5 rupee margin
        BigDecimal difference = request.getTotalAmount().subtract(calculatedTotal).abs();

        if (difference.compareTo(margin) > 0) {
            throw new ValidationException("Total amount must be within 5 rupees of calculated total (subtotal + tax + tip). " +
                    "Expected: " + calculatedTotal + ", Actual: " + request.getTotalAmount() +
                    ", Difference: " + difference);
        }
    }

    private ExpenseResponse mapToExpenseResponse(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .restaurantName(expense.getRestaurantName())
                .slug(expense.getSlug())
                .createdAt(expense.getCreatedAt())
                .payerName(expense.getPayerName())
                .totalAmount(expense.getTotalAmount())
                .subtotal(expense.getSubtotal())
                .tax(expense.getTax())
                .serviceCharge(expense.getServiceCharge())
                .discount(expense.getDiscount())
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
                .quantity(item.getQuantity())
                .totalQuantity(item.getTotalQuantity())
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
                .serviceChargeShare(person.getServiceChargeShare())
                .discountShare(person.getDiscountShare())
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
                .serviceChargeShare(personRequest.getServiceChargeShare() != null ? personRequest.getServiceChargeShare() : BigDecimal.ZERO)
                .totalOwed(personRequest.getTotalOwed() != null ? personRequest.getTotalOwed() : BigDecimal.ZERO)
                .isFinished(personRequest.isFinished())
                .itemsClaimed(new ArrayList<>())
                .build();

        expense.addPerson(person);

        Expense savedExpense = expenseRepository.save(expense);
        return mapToExpenseResponse(savedExpense);
    }

    @Transactional
    public ExpenseResponse createExpenseFromImage(byte[] imageData, String payerName) {
        try {
            String geminiResponse = geminiService.getGeminiResponse(imageData);
            ObjectMapper objectMapper = new ObjectMapper();
            BillParsedData billParsedData = objectMapper.readValue(geminiResponse, BillParsedData.class);
            ExpenseRequest expenseRequest = createExpenseRequestFromParsedData(billParsedData, payerName);

            return createExpense(expenseRequest);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create expense from image: " + e.getMessage(), e);
        }
    }

    private ExpenseRequest createExpenseRequestFromParsedData(BillParsedData parsedData, String payerName) {
        List<ItemRequest> itemRequests = parsedData.getItems().stream()
                .flatMap(billItem -> {
                    BigDecimal unitPrice = billItem.getPrice().divide(new BigDecimal(billItem.getQuantity()), RoundingMode.CEILING);
                    return IntStream.range(0, billItem.getQuantity())
                            .mapToObj(i -> ItemRequest.builder()
                                    .name(billItem.getName())
                                    .price(unitPrice)
                                    .quantity(1)
                                    .totalQuantity(billItem.getQuantity())
                                    .build()
                            );
                })
                .collect(Collectors.toList());

        return ExpenseRequest.builder()
                .subtotal(parsedData.getSubtotal())
                .restaurantName(parsedData.getRestaurantName())
                .tax(parsedData.getTax())
                .serviceCharge(parsedData.getServiceCharge())
                .totalAmount(parsedData.getTotalAmount())
                .items(itemRequests)
                .payerName(payerName)
                .people(new ArrayList<>()) // No people initially, they can be added later
                .build();
    }
}