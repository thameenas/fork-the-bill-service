package com.forkthebill.service.repositories;

import com.forkthebill.service.models.entities.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    Optional<Expense> findBySlug(String slug);
    boolean existsBySlug(String slug);
}