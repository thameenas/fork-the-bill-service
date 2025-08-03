package com.forkthebill.service.utils;

import com.forkthebill.service.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SlugGeneratorTest {

    @Mock
    private ExpenseRepository expenseRepository;

    private SlugGenerator slugGenerator;

    @BeforeEach
    public void setup() {
        slugGenerator = new SlugGenerator(expenseRepository);
    }

    @Test
    public void generateUniqueSlug_shouldReturnSlugWithThreeWords() {
        // Given
        when(expenseRepository.existsBySlug(anyString())).thenReturn(false);

        // When
        String slug = slugGenerator.generateUniqueSlug();

        // Then
        assertThat(slug).isNotNull();
        String[] parts = slug.split("-");
        assertThat(parts).hasSize(3);
        
        // Each word should be 3-4 characters
        for (String part : parts) {
            assertThat(part.length()).isBetween(3, 4);
        }
    }

    @Test
    public void generateUniqueSlug_shouldAddRandomNumberWhenMaxAttemptsReached() {
        // Given
        when(expenseRepository.existsBySlug(anyString())).thenReturn(true, true, true, true, true, 
                                                                    true, true, true, true, true, false);

        // When
        String slug = slugGenerator.generateUniqueSlug();

        // Then
        assertThat(slug).isNotNull();
        
        // Should contain a number at the end after 10 attempts
        String[] parts = slug.split("-");
        assertThat(parts).hasSizeGreaterThan(3);
        
        // Last part should be a number
        String lastPart = parts[parts.length - 1];
        assertThat(lastPart).matches("\\d+");
    }
}