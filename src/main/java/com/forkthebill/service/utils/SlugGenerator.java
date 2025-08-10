package com.forkthebill.service.utils;

import com.forkthebill.service.repositories.ExpenseRepository;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.security.SecureRandom;
import java.util.Random;

@Component
public class SlugGenerator {
    private final ExpenseRepository expenseRepository;
    private final Random random = new SecureRandom();
    private final List<String> words;

    public SlugGenerator(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
        this.words = loadWords();
    }

    private List<String> loadWords() {
        try {
            ClassPathResource resource = new ClassPathResource("words.txt");
            return Files.readAllLines(resource.getFile().toPath());
        } catch (IOException e) {
            throw new RuntimeException("Failed to load words.txt", e);
        }
    }

    public String generateUniqueSlug() {
        String slug;
        int attempts = 0;
        final int MAX_ATTEMPTS = 10;

        do {
            slug = generateSlug();
            attempts++;

            if (attempts >= MAX_ATTEMPTS) {
                // Add random suffix to ensure uniqueness
                slug = slug + "-" + random.nextInt(1000);
                break;
            }
        } while (expenseRepository.existsBySlug(slug));

        return slug;
    }

    private String generateSlug() {
        String word1 = words.get(random.nextInt(words.size()));
        String word2 = words.get(random.nextInt(words.size()));
        String word3 = words.get(random.nextInt(words.size()));
        
        return word1 + "-" + word2 + "-" + word3;
    }
}