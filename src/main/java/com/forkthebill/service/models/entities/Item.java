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
@Table(name = "items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Item {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "expense_id")
    @ToString.Exclude
    private Expense expense;
    
    @ElementCollection
    @CollectionTable(name = "item_claimed_by", joinColumns = @JoinColumn(name = "item_id"))
    @Column(name = "person_id")
    @Builder.Default
    private List<UUID> claimedBy = new ArrayList<>();
}