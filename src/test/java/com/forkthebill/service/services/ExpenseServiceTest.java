package com.forkthebill.service.services;

import com.forkthebill.service.exceptions.ResourceNotFoundException;
import com.forkthebill.service.exceptions.ValidationException;
import com.forkthebill.service.models.dto.ExpenseRequest;
import com.forkthebill.service.models.dto.ExpenseResponse;
import com.forkthebill.service.models.dto.ItemRequest;
import com.forkthebill.service.models.dto.ItemResponse;
import com.forkthebill.service.models.dto.PersonResponse;
import com.forkthebill.service.models.entities.Expense;
import com.forkthebill.service.models.entities.Item;
import com.forkthebill.service.models.entities.Person;
import com.forkthebill.service.repositories.ExpenseRepository;
import com.forkthebill.service.utils.SlugGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.never;
import com.forkthebill.service.models.dto.PersonRequest;
import java.util.Arrays;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SlugGenerator slugGenerator;

    @Captor
    private ArgumentCaptor<Expense> expenseCaptor;

    @InjectMocks
    private ExpenseService expenseService;

    @BeforeEach
    public void setup() {
        expenseService = new ExpenseService(expenseRepository, slugGenerator);
    }

    @Test
    public void createExpense_shouldCreateAndReturnExpense() {
        // Given
        String slug = "test-slug";
        when(slugGenerator.generateUniqueSlug()).thenReturn(slug);
        
        ExpenseRequest request = createValidExpenseRequest();
        
        Expense savedExpense = Expense.builder()
                .id("1")
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .payerName(request.getPayerName())
                .totalAmount(request.getTotalAmount())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .tip(request.getTip())
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();
        
        when(expenseRepository.save(any(Expense.class))).thenReturn(savedExpense);

        // When
        ExpenseResponse response = expenseService.createExpense(request);

        // Then
        verify(expenseRepository).save(expenseCaptor.capture());
        Expense capturedExpense = expenseCaptor.getValue();
        
        assertThat(capturedExpense.getPayerName()).isEqualTo(request.getPayerName());
        assertThat(capturedExpense.getTotalAmount()).isEqualByComparingTo(request.getTotalAmount());
        assertThat(capturedExpense.getSubtotal()).isEqualByComparingTo(request.getSubtotal());
        assertThat(capturedExpense.getTax()).isEqualByComparingTo(request.getTax());
        assertThat(capturedExpense.getTip()).isEqualByComparingTo(request.getTip());
        assertThat(capturedExpense.getSlug()).isEqualTo(slug);
        assertThat(capturedExpense.getItems()).hasSize(1);
        
        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getSlug()).isEqualTo(slug);
        assertThat(response.getPayerName()).isEqualTo(request.getPayerName());
        assertThat(response.getTotalAmount()).isEqualByComparingTo(request.getTotalAmount());
    }
    
    @Test
    public void createExpense_shouldThrowException_whenTotalAmountDoesNotMatchCalculatedTotal() {
        // Given
        ExpenseRequest request = ExpenseRequest.builder()
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("5.00")) // This makes the total 95, not 100
                .items(List.of(
                        ItemRequest.builder()
                                .name("Burger")
                                .price(new BigDecimal("80.00"))
                                .build()
                ))
                .build();

        // When/Then
        assertThatThrownBy(() -> expenseService.createExpense(request))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Total amount must equal subtotal + tax + tip");
    }
    
    @Test
    public void getExpenseBySlug_shouldReturnExpense_whenExpenseExists() {
        // Given
        String slug = "test-slug";
        Expense expense = Expense.builder()
                .id("1")
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("10.00"))
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        
        // When
        ExpenseResponse response = expenseService.getExpenseBySlug(slug);
        
        // Then
        assertThat(response.getId()).isEqualTo("1");
        assertThat(response.getSlug()).isEqualTo(slug);
        assertThat(response.getPayerName()).isEqualTo("John Doe");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
    
    @Test
    public void getExpenseBySlug_shouldThrowException_whenExpenseDoesNotExist() {
        // Given
        String slug = "non-existent-slug";
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> expenseService.getExpenseBySlug(slug))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found with slug: " + slug);
    }
    
    @Test
    public void updateExpenseBySlug_shouldUpdateAndReturnExpense_whenExpenseExists() {
        // Given
        String slug = "test-slug";
        ExpenseRequest updateRequest = ExpenseRequest.builder()
                .payerName("Jane Doe") // Changed from John Doe
                .totalAmount(new BigDecimal("120.00")) // Changed from 100.00
                .subtotal(new BigDecimal("100.00")) // Changed from 80.00
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("10.00"))
                .items(List.of(
                        ItemRequest.builder()
                                .name("Pizza")
                                .price(new BigDecimal("100.00"))
                                .build()
                ))
                .build();
        
        Expense existingExpense = Expense.builder()
                .id("1")
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("10.00"))
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();
        
        Item item = Item.builder()
                .name("Burger")
                .price(new BigDecimal("80.00"))
                .claimedBy(new ArrayList<>())
                .build();
        existingExpense.addItem(item);
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(existingExpense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(existingExpense);
        
        // When
        ExpenseResponse response = expenseService.updateExpenseBySlug(slug, updateRequest);
        
        // Then
        verify(expenseRepository).save(expenseCaptor.capture());
        Expense capturedExpense = expenseCaptor.getValue();
        
        assertThat(capturedExpense.getPayerName()).isEqualTo("Jane Doe");
        assertThat(capturedExpense.getTotalAmount()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(capturedExpense.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
        assertThat(capturedExpense.getItems()).hasSize(1);
        assertThat(capturedExpense.getItems().get(0).getName()).isEqualTo("Pizza");
        assertThat(capturedExpense.getItems().get(0).getPrice()).isEqualByComparingTo(new BigDecimal("100.00"));
        
        assertThat(response.getPayerName()).isEqualTo("Jane Doe");
        assertThat(response.getTotalAmount()).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(response.getSubtotal()).isEqualByComparingTo(new BigDecimal("100.00"));
    }
    
    @Test
    public void updateExpenseBySlug_shouldThrowException_whenExpenseDoesNotExist() {
        // Given
        String slug = "non-existent-slug";
        ExpenseRequest updateRequest = createValidExpenseRequest();
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.empty());
        
        // When/Then
        assertThatThrownBy(() -> expenseService.updateExpenseBySlug(slug, updateRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Expense not found with slug: " + slug);
    }
    
    @Test
    public void updateExpenseBySlug_shouldThrowException_whenTotalAmountDoesNotMatchCalculatedTotal() {
        // Given
        String slug = "test-slug";
        ExpenseRequest updateRequest = ExpenseRequest.builder()
                .payerName("Jane Doe")
                .totalAmount(new BigDecimal("120.00"))
                .subtotal(new BigDecimal("100.00"))
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("5.00")) // This makes the total 115, not 120
                .items(List.of(
                        ItemRequest.builder()
                                .name("Pizza")
                                .price(new BigDecimal("100.00"))
                                .build()
                ))
                .build();
        
        // When/Then
        assertThatThrownBy(() -> expenseService.updateExpenseBySlug(slug, updateRequest))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Total amount must equal subtotal + tax + tip");
    }
    
    @Test
    public void claimItem_ShouldAddPersonToItemClaimedByList() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        
        // When
        ExpenseResponse response = expenseService.claimItem(expense.getSlug(), itemId, personId);
        
        // Then
        ItemResponse claimedItem = response.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow();
        assertTrue(claimedItem.getClaimedBy().contains(personId));
    }

    @Test
    public void claimItem_ShouldAddItemToPersonItemsClaimedList() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        
        // When
        ExpenseResponse response = expenseService.claimItem(expense.getSlug(), itemId, personId);
        
        // Then
        PersonResponse claimingPerson = response.getPeople().stream()
                .filter(person -> person.getId().equals(personId))
                .findFirst()
                .orElseThrow();
        assertTrue(claimingPerson.getItemsClaimed().contains(itemId));
    }

    @Test
    public void claimItem_ShouldRecalculateAmounts() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        
        // When
        ExpenseResponse response = expenseService.claimItem(expense.getSlug(), itemId, personId);
        
        // Then
        PersonResponse claimingPerson = response.getPeople().stream()
                .filter(person -> person.getId().equals(personId))
                .findFirst()
                .orElseThrow();
        assertEquals(new BigDecimal("80.00"), claimingPerson.getSubtotal());
        assertEquals(new BigDecimal("100.00"), claimingPerson.getTotalOwed());
        assertTrue(expense.getItems().get(0).getClaimedBy().contains(claimingPerson.getId()));
    }

    @Test
    public void unclaimItem_ShouldRemovePersonFromItemClaimedByList() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        // First claim the item
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        expenseService.claimItem(expense.getSlug(), itemId, personId);
        
        // When
        ExpenseResponse response = expenseService.unclaimItem(expense.getSlug(), itemId, personId);
        
        // Then
        ItemResponse unclaimedItem = response.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow();
        assertFalse(unclaimedItem.getClaimedBy().contains(personId));
    }

    @Test
    public void unclaimItem_ShouldRemoveItemFromPersonItemsClaimedList() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        // First claim the item
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        expenseService.claimItem(expense.getSlug(), itemId, personId);
        
        // When
        ExpenseResponse response = expenseService.unclaimItem(expense.getSlug(), itemId, personId);
        
        // Then
        PersonResponse unclaimingPerson = response.getPeople().stream()
                .filter(person -> person.getId().equals(personId))
                .findFirst()
                .orElseThrow();
        assertFalse(unclaimingPerson.getItemsClaimed().contains(itemId));
    }

    @Test
    public void unclaimItem_ShouldRecalculateAmounts() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        // First claim the item
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        expenseService.claimItem(expense.getSlug(), itemId, personId);
        
        // When
        ExpenseResponse response = expenseService.unclaimItem(expense.getSlug(), itemId, personId);
        
        // Then
        PersonResponse unclaimingPerson = response.getPeople().stream()
                .filter(person -> person.getId().equals(personId))
                .findFirst()
                .orElseThrow();
        assertEquals(BigDecimal.ZERO, unclaimingPerson.getSubtotal());
    }

    @Test
    public void claimItem_ShouldThrowException_WhenExpenseNotFound() {
        // Given
        String nonExistentSlug = "non-existent-slug";
        String itemId = "item1";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        
        when(expenseRepository.findBySlug(nonExistentSlug)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> 
            expenseService.claimItem(nonExistentSlug, itemId, personId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Expense not found with slug: " + nonExistentSlug);
    }

    @Test
    public void unclaimItem_ShouldThrowException_WhenExpenseNotFound() {
        // Given
        String nonExistentSlug = "non-existent-slug";
        String itemId = "item1";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440001");
        
        when(expenseRepository.findBySlug(nonExistentSlug)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> 
            expenseService.unclaimItem(nonExistentSlug, itemId, personId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Expense not found with slug: " + nonExistentSlug);
    }

    @Test
    public void claimItem_ShouldThrowException_WhenItemAlreadyClaimed() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        // Pre-claim the item
        expense.getItems().get(0).getClaimedBy().add(personId);
        expense.getPeople().get(0).getItemsClaimed().add(itemId);
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            expenseService.claimItem(expense.getSlug(), itemId, personId);
        });
    }

    @Test
    public void claimItem_ShouldThrowException_WhenPersonAlreadyClaimedItem() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        // Pre-claim the item
        expense.getItems().get(0).getClaimedBy().add(personId);
        expense.getPeople().get(0).getItemsClaimed().add(itemId);
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            expenseService.claimItem(expense.getSlug(), itemId, personId);
        });
    }

    @Test
    public void unclaimItem_ShouldThrowException_WhenItemNotClaimed() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            expenseService.unclaimItem(expense.getSlug(), itemId, personId);
        });
    }

    @Test
    public void unclaimItem_ShouldThrowException_WhenPersonNotClaimedItem() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID personId = expense.getPeople().get(0).getId();
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        
        // When & Then
        assertThrows(ValidationException.class, () -> {
            expenseService.unclaimItem(expense.getSlug(), itemId, personId);
        });
    }

    @Test
    public void claimItem_ShouldHandleMultiplePeopleClaimingSameItem() {
        // Given
        Expense expense = createTestExpense();
        String itemId = expense.getItems().get(0).getId();
        UUID person1Id = expense.getPeople().get(0).getId();
        
        // Add a second person
        Person person2 = Person.builder()
                .id(UUID.randomUUID())
                .name("Person 2")
                .itemsClaimed(new ArrayList<>())
                .amountOwed(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .taxShare(BigDecimal.ZERO)
                .tipShare(BigDecimal.ZERO)
                .totalOwed(BigDecimal.ZERO)
                .isFinished(false)
                .build();
        expense.addPerson(person2);
        
        when(expenseRepository.findBySlug(expense.getSlug())).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        
        // When - both people claim the same item
        expenseService.claimItem(expense.getSlug(), itemId, person1Id);
        expenseService.claimItem(expense.getSlug(), itemId, person2.getId());
        
        // Then - both should be in the claimedBy list
        ItemResponse claimedItem = expenseService.getExpenseBySlug(expense.getSlug())
                .getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow();
        
        assertTrue(claimedItem.getClaimedBy().contains(person1Id));
        assertTrue(claimedItem.getClaimedBy().contains(person2.getId()));
        assertEquals(2, claimedItem.getClaimedBy().size());
    }
    
    private ExpenseRequest createValidExpenseRequest() {
        return ExpenseRequest.builder()
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("10.00"))
                .items(List.of(
                        ItemRequest.builder()
                                .name("Burger")
                                .price(new BigDecimal("80.00"))
                                .build()
                ))
                .build();
    }

    private Expense createTestExpense() {
        Expense expense = Expense.builder()
                .id("1")
                .slug("test-expense")
                .createdAt(LocalDateTime.now())
                .payerName("John Doe")
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("10.00"))
                .totalAmount(new BigDecimal("100.00"))
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();

        Item item = Item.builder()
                .id("item1")
                .name("Item 1")
                .price(new BigDecimal("80.00"))
                .claimedBy(new ArrayList<>())
                .build();
        expense.addItem(item);

        Person person = Person.builder()
                .id(UUID.randomUUID())
                .name("Person 1")
                .itemsClaimed(new ArrayList<>())
                .amountOwed(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .taxShare(BigDecimal.ZERO)
                .tipShare(BigDecimal.ZERO)
                .totalOwed(BigDecimal.ZERO)
                .isFinished(false)
                .build();
        expense.addPerson(person);

        return expense;
    }

    @Test
    public void markPersonAsFinished_shouldMarkPersonAsFinished_whenValidSlugAndPersonId() {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440002");
        
        Expense expense = createTestExpense();
        Person person = createTestPerson(personId, "John Doe");
        expense.addPerson(person);
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        
        // When
        expenseService.markPersonAsFinished(slug, personId);
        
        // Then
        verify(expenseRepository).save(expense);
        Expense savedExpense = expenseRepository.save(expense);
        Person savedPerson = savedExpense.findPersonById(personId);
        assertTrue(savedPerson.isFinished());
    }
    
    @Test
    public void markPersonAsFinished_shouldThrowResourceNotFoundException_whenExpenseNotFound() {
        // Given
        String slug = "non-existent-slug";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440003");
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> 
            expenseService.markPersonAsFinished(slug, personId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Expense not found with slug: " + slug);
    }

    @Test
    public void markPersonAsFinished_shouldThrowResourceNotFoundException_whenPersonNotFound() {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440004");
        
        Expense expense = createTestExpense();
        Person person = createTestPerson(UUID.fromString("550e8400-e29b-41d4-a716-446655440005"), "John Doe");
        expense.addPerson(person);
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        
        // When & Then
        assertThatThrownBy(() -> 
            expenseService.markPersonAsFinished(slug, personId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Person not found with ID: " + personId);
    }
    
    @Test
    public void markPersonAsPending_shouldMarkPersonAsPending_whenValidSlugAndPersonId() {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");
        
        Expense expense = createTestExpense();
        Person person = createTestPerson(personId, "John Doe");
        expense.addPerson(person);
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);
        
        // When
        expenseService.markPersonAsPending(slug, personId);
        
        // Then
        verify(expenseRepository).save(expense);
        Expense savedExpense = expenseRepository.save(expense);
        Person savedPerson = savedExpense.findPersonById(personId);
        assertFalse(savedPerson.isFinished());
    }
    
    @Test
    public void markPersonAsPending_shouldThrowResourceNotFoundException_whenExpenseNotFound() {
        // Given
        String slug = "non-existent-slug";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> 
            expenseService.markPersonAsPending(slug, personId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Expense not found with slug: " + slug);
    }

    @Test
    public void markPersonAsPending_shouldThrowResourceNotFoundException_whenPersonNotFound() {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440008");
        
        Expense expense = createTestExpense();
        Person person = createTestPerson(UUID.fromString("550e8400-e29b-41d4-a716-446655440009"), "John Doe");
        expense.addPerson(person);
        
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        
        // When & Then
        assertThatThrownBy(() -> 
            expenseService.markPersonAsPending(slug, personId))
            .isInstanceOf(ResourceNotFoundException.class)
            .hasMessage("Person not found with ID: " + personId);
    }
    
    private Person createTestPerson(UUID id, String name) {
        Person person = Person.builder()
                .id(id)
                .name(name)
                .amountOwed(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .taxShare(BigDecimal.ZERO)
                .tipShare(BigDecimal.ZERO)
                .totalOwed(BigDecimal.ZERO)
                .isFinished(false)
                .itemsClaimed(new ArrayList<>())
                .build();
        return person;
    }

    @Test
    void markPersonAsPending_WhenExpenseNotFound_ThrowsResourceNotFoundException() {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.fromString("550e8400-e29b-41d4-a716-446655440010");
        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> expenseService.markPersonAsPending(slug, personId));
    }

    @Test
    void addPersonToExpense_WhenExpenseExists_AddsPersonSuccessfully() {
        // Given
        String slug = "test-slug";
        PersonRequest personRequest = PersonRequest.builder()
                .name("John Doe")
                .amountOwed(new BigDecimal("15.00"))
                .isFinished(false)
                .build();

        Expense expense = Expense.builder()
                .id("expense-1")
                .slug(slug)
                .payerName("Jane")
                .totalAmount(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("40.00"))
                .tax(new BigDecimal("5.00"))
                .tip(new BigDecimal("5.00"))
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();

        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        // When
        ExpenseResponse result = expenseService.addPersonToExpense(slug, personRequest);

        // Then
        assertNotNull(result);
        assertEquals(slug, result.getSlug());
        assertEquals(1, result.getPeople().size());
        assertEquals("John Doe", result.getPeople().get(0).getName());
        assertEquals(new BigDecimal("15.00"), result.getPeople().get(0).getAmountOwed());
        assertFalse(result.getPeople().get(0).isFinished());

        verify(expenseRepository).findBySlug(slug);
        verify(expenseRepository).save(expense);
    }

    @Test
    void addPersonToExpense_WhenExpenseNotFound_ThrowsResourceNotFoundException() {
        // Given
        String slug = "non-existent-slug";
        PersonRequest personRequest = PersonRequest.builder()
                .name("John Doe")
                .build();

        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.empty());

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> expenseService.addPersonToExpense(slug, personRequest));
        verify(expenseRepository).findBySlug(slug);
        verify(expenseRepository, never()).save(any());
    }

    @Test
    void addPersonToExpense_WithNullOptionalFields_SetsDefaultValues() {
        // Given
        String slug = "test-slug";
        PersonRequest personRequest = PersonRequest.builder()
                .name("John Doe")
                .build(); // All optional fields are null

        Expense expense = Expense.builder()
                .id("expense-1")
                .slug(slug)
                .payerName("Jane")
                .totalAmount(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("40.00"))
                .tax(new BigDecimal("5.00"))
                .tip(new BigDecimal("5.00"))
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();

        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        // When
        ExpenseResponse result = expenseService.addPersonToExpense(slug, personRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPeople().size());
        PersonResponse addedPerson = result.getPeople().get(0);
        assertEquals("John Doe", addedPerson.getName());
        assertEquals(BigDecimal.ZERO, addedPerson.getAmountOwed());
        assertEquals(BigDecimal.ZERO, addedPerson.getSubtotal());
        assertEquals(BigDecimal.ZERO, addedPerson.getTaxShare());
        assertEquals(BigDecimal.ZERO, addedPerson.getTipShare());
        assertEquals(BigDecimal.ZERO, addedPerson.getTotalOwed());
        assertFalse(addedPerson.isFinished());
        assertTrue(addedPerson.getItemsClaimed().isEmpty());

        verify(expenseRepository).findBySlug(slug);
        verify(expenseRepository).save(expense);
    }

    @Test
    void addPersonToExpense_WithAllFieldsProvided_SetsAllValuesCorrectly() {
        // Given
        String slug = "test-slug";
        PersonRequest personRequest = PersonRequest.builder()
                .name("John Doe")
                .amountOwed(new BigDecimal("20.00"))
                .subtotal(new BigDecimal("15.00"))
                .taxShare(new BigDecimal("2.00"))
                .tipShare(new BigDecimal("3.00"))
                .totalOwed(new BigDecimal("20.00"))
                .isFinished(true)
                .itemsClaimed(Arrays.asList("item-1", "item-2"))
                .build();

        Expense expense = Expense.builder()
                .id("expense-1")
                .slug(slug)
                .payerName("Jane")
                .totalAmount(new BigDecimal("50.00"))
                .subtotal(new BigDecimal("40.00"))
                .tax(new BigDecimal("5.00"))
                .tip(new BigDecimal("5.00"))
                .createdAt(LocalDateTime.now())
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();

        when(expenseRepository.findBySlug(slug)).thenReturn(Optional.of(expense));
        when(expenseRepository.save(any(Expense.class))).thenReturn(expense);

        // When
        ExpenseResponse result = expenseService.addPersonToExpense(slug, personRequest);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPeople().size());
        PersonResponse addedPerson = result.getPeople().get(0);
        assertEquals("John Doe", addedPerson.getName());
        assertEquals(new BigDecimal("20.00"), addedPerson.getAmountOwed());
        assertEquals(new BigDecimal("15.00"), addedPerson.getSubtotal());
        assertEquals(new BigDecimal("2.00"), addedPerson.getTaxShare());
        assertEquals(new BigDecimal("3.00"), addedPerson.getTipShare());
        assertEquals(new BigDecimal("20.00"), addedPerson.getTotalOwed());
        assertTrue(addedPerson.isFinished());
        // Note: itemsClaimed will be empty because we create a new Person with empty list
        assertTrue(addedPerson.getItemsClaimed().isEmpty());

        verify(expenseRepository).findBySlug(slug);
        verify(expenseRepository).save(expense);
    }
}