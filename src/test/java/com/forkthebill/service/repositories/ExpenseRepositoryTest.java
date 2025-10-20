package com.forkthebill.service.repositories;

import com.forkthebill.service.models.entities.Expense;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
public class ExpenseRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ExpenseRepository expenseRepository;

    @Test
    public void findBySlug_shouldReturnExpense_whenSlugExists() {
        // Given
        Expense expense = new Expense();
        expense.setSlug("test-slug");
        expense.setCreatedAt(LocalDateTime.now());
        expense.setPayerName("John Doe");
        expense.setTotalAmount(new BigDecimal("100.00"));
        expense.setSubtotal(new BigDecimal("80.00"));
        expense.setTax(new BigDecimal("10.00"));
        expense.setServiceCharge(new BigDecimal("10.00"));
        
        entityManager.persist(expense);
        entityManager.flush();
        
        // When
        Optional<Expense> found = expenseRepository.findBySlug("test-slug");
        
        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getSlug()).isEqualTo("test-slug");
        assertThat(found.get().getPayerName()).isEqualTo("John Doe");
    }
    
    @Test
    public void findBySlug_shouldReturnEmpty_whenSlugDoesNotExist() {
        // When
        Optional<Expense> found = expenseRepository.findBySlug("non-existent-slug");
        
        // Then
        assertThat(found).isEmpty();
    }
    
    @Test
    public void existsBySlug_shouldReturnTrue_whenSlugExists() {
        // Given
        Expense expense = new Expense();
        expense.setSlug("test-slug-2");
        expense.setCreatedAt(LocalDateTime.now());
        expense.setPayerName("Jane Doe");
        expense.setTotalAmount(new BigDecimal("200.00"));
        expense.setSubtotal(new BigDecimal("160.00"));
        expense.setTax(new BigDecimal("20.00"));
        expense.setServiceCharge(new BigDecimal("20.00"));
        
        entityManager.persist(expense);
        entityManager.flush();
        
        // When
        boolean exists = expenseRepository.existsBySlug("test-slug-2");
        
        // Then
        assertThat(exists).isTrue();
    }
    
    @Test
    public void existsBySlug_shouldReturnFalse_whenSlugDoesNotExist() {
        // When
        boolean exists = expenseRepository.existsBySlug("non-existent-slug");
        
        // Then
        assertThat(exists).isFalse();
    }
}