package com.forkthebill.service.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "expenses")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Expense {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(unique = true, nullable = false)
    private String slug;
    
    @Column(nullable = false)
    private LocalDateTime createdAt;
    
    @Column(nullable = false)
    private String payerName;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tax;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tip;
    
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<Item> items = new ArrayList<>();
    
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    private List<Person> people = new ArrayList<>();
    
    // Helper methods
    public void addItem(Item item) {
        items.add(item);
        item.setExpense(this);
    }
    
    public void removeItem(Item item) {
        items.remove(item);
        item.setExpense(null);
    }
    
    public void addPerson(Person person) {
        people.add(person);
        person.setExpense(this);
    }
    
    public void removePerson(Person person) {
        people.remove(person);
        person.setExpense(null);
    }
}