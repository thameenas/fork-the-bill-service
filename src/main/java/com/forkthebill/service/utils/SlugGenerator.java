package com.forkthebill.service.utils;

import com.forkthebill.service.repositories.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class SlugGenerator {
    private final ExpenseRepository expenseRepository;
    private final Random random = new SecureRandom();
    
    private static final String CONSONANTS = "bcdfghjklmnpqrstvwxyz";
    private static final String VOWELS = "aeiou";
    
    public String generateUniqueSlug() {
        String slug;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;
        
        do {
            slug = generateSlug();
            attempts++;
            
            if (attempts >= MAX_ATTEMPTS) {
                slug = slug + "-" + random.nextInt(1000);
            }
        } while (expenseRepository.existsBySlug(slug));
        
        return slug;
    }
    
    private String generateSlug() {
        String word1 = generateRandomWord(3, 4);
        String word2 = generateRandomWord(3, 4);
        String word3 = generateRandomWord(3, 4);
        
        return word1 + "-" + word2 + "-" + word3;
    }
    
    private String generateRandomWord(int minLength, int maxLength) {
        int length = random.nextInt(maxLength - minLength + 1) + minLength;
        StringBuilder word = new StringBuilder(length);
        
        boolean useConsonant = random.nextBoolean();
        
        for (int i = 0; i < length; i++) {
            if (useConsonant) {
                word.append(CONSONANTS.charAt(random.nextInt(CONSONANTS.length())));
            } else {
                word.append(VOWELS.charAt(random.nextInt(VOWELS.length())));
            }
            
            useConsonant = !useConsonant;
        }
        
        return word.toString();
    }
}