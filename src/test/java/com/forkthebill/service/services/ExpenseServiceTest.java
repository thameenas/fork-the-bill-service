package com.forkthebill.service.services;

import com.forkthebill.service.exceptions.ResourceNotFoundException;
import com.forkthebill.service.exceptions.ValidationException;
import com.forkthebill.service.models.dto.ExpenseRequest;
import com.forkthebill.service.models.dto.ExpenseResponse;
import com.forkthebill.service.models.dto.ItemRequest;
import com.forkthebill.service.models.entities.Expense;
import com.forkthebill.service.repositories.ExpenseRepository;
import com.forkthebill.service.utils.SlugGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private SlugGenerator slugGenerator;

    @Captor
    private ArgumentCaptor<Expense> expenseCaptor;

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
}