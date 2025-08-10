package com.forkthebill.service.utils;

import com.forkthebill.service.repositories.ExpenseRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.List;

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
        List<String> testWords = Arrays.asList("cat", "dog", "bird", "fish", "tree", "book", "car", "house", 
                                              "sun", "moon", "star", "cloud", "rain", "snow", "wind", "fire", 
                                              "water", "earth", "sky", "sea");
        
        slugGenerator = new SlugGenerator(expenseRepository);
        ReflectionTestUtils.setField(slugGenerator, "words", testWords);
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
        
        // Each word should be from our test word list
        List<String> validWords = Arrays.asList("cat", "dog", "bird", "fish", "tree", "book", "car", "house", 
                                               "sun", "moon", "star", "cloud", "rain", "snow", "wind", "fire", 
                                               "water", "earth", "sky", "sea");
        for (String part : parts) {
            assertThat(validWords).contains(part);
        }
    }

    @Test
    public void generateUniqueSlug_shouldAddRandomNumberWhenMaxAttemptsReached() {
        // Given
        when(expenseRepository.existsBySlug(anyString())).thenReturn(true);

        // When
        String slug = slugGenerator.generateUniqueSlug();

        // Then
        assertThat(slug).isNotNull();
        
        // Should contain a number at the end after max attempts
        String[] parts = slug.split("-");
        assertThat(parts).hasSizeGreaterThan(3);
        
        // Last part should be a number
        String lastPart = parts[parts.length - 1];
        assertThat(lastPart).matches("\\d+");
        
        // Should be a 4-digit number (0-9999)
        int number = Integer.parseInt(lastPart);
        assertThat(number).isBetween(0, 9999);
    }

    @Test
    public void generateUniqueSlug_shouldReturnUniqueSlugWhenRepositoryReturnsFalse() {
        // Given
        when(expenseRepository.existsBySlug(anyString())).thenReturn(false);

        // When
        String slug1 = slugGenerator.generateUniqueSlug();
        String slug2 = slugGenerator.generateUniqueSlug();

        // Then
        assertThat(slug1).isNotEqualTo(slug2);
        assertThat(slug1).isNotNull();
        assertThat(slug2).isNotNull();
    }

    @Test
    public void generateSlug_shouldReturnThreeWordsSeparatedByHyphens() {
        // Given
        when(expenseRepository.existsBySlug(anyString())).thenReturn(false);

        // When
        String slug = slugGenerator.generateUniqueSlug();

        // Then
        assertThat(slug).contains("-");
        String[] parts = slug.split("-");
        assertThat(parts).hasSize(3);
        
        // All parts should be non-empty
        for (String part : parts) {
            assertThat(part).isNotEmpty();
        }
    }

    @Test
    public void constructor_shouldNotThrowException() {
        // Given & When & Then
        assertThat(new SlugGenerator(expenseRepository)).isNotNull();
    }
}