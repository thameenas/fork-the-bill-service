package com.forkthebill.service.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "people")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Person {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    @ToString.Exclude
    private Expense expense;
    
    @ElementCollection
    @CollectionTable(name = "person_items_claimed", joinColumns = @JoinColumn(name = "person_id"))
    @Column(name = "item_id")
    @Builder.Default
    private List<String> itemsClaimed = new ArrayList<>();
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amountOwed;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal taxShare;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tipShare;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalOwed;
    
    @Column(nullable = false)
    @Builder.Default
    private boolean isFinished = false;
}