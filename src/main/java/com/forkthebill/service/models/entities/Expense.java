package com.forkthebill.service.models.entities;

import com.forkthebill.service.exceptions.ResourceNotFoundException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

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

    @Column
    private String restaurantName;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    private BigDecimal tax;

    private BigDecimal serviceCharge;

    private BigDecimal discount;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    @ToString.Exclude
    @Builder.Default
    @OrderColumn(name = "item_order")
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

    public Person findPersonById(UUID personId) {
        return people.stream()
                .filter(p -> p.getId().equals(personId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Person not found with ID: " + personId));
    }

    public Item findItemById(String itemId) {
        return items.stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Item not found with ID: " + itemId));
    }

    public void claimItem(String itemId, UUID personId) {
        Person person = findPersonById(personId);
        Item item = findItemById(itemId);

        // Add claim
        if (!item.getClaimedBy().contains(personId)) {
            item.getClaimedBy().add(personId);
        }

        if (!person.getItemsClaimed().contains(itemId)) {
            person.getItemsClaimed().add(itemId);
        }

        // Recalculate amounts
        recalculateAmounts();
    }

    public void unclaimItem(String itemId, UUID personId) {
        Person person = findPersonById(personId);
        Item item = findItemById(itemId);

        // Remove claim
        item.getClaimedBy().remove(personId);
        person.getItemsClaimed().remove(itemId);

        // Recalculate amounts
        recalculateAmounts();
    }

    private BigDecimal calculatePersonSubtotal(Person person) {
        BigDecimal personSubtotal = BigDecimal.ZERO;

        for (String itemId : person.getItemsClaimed()) {
            Optional<Item> itemOpt = items.stream()
                    .filter(i -> i.getId().equals(itemId))
                    .findFirst();

            if (itemOpt.isPresent()) {
                Item item = itemOpt.get();
                int claimCount = item.getClaimedBy().size();

                if (claimCount > 0) {
                    // Divide item price by number of people claiming it
                    BigDecimal priceShare = item.getPrice()
                            .divide(BigDecimal.valueOf(claimCount), 2, RoundingMode.HALF_UP);
                    personSubtotal = personSubtotal.add(priceShare);
                }
            }
        }

        return personSubtotal;
    }

    public void recalculateAmounts() {
        for (Person person : people) {
            // Calculate subtotal for this person
            BigDecimal personSubtotal = calculatePersonSubtotal(person);
            person.setSubtotal(personSubtotal);

            // Calculate tax and service charge shares proportionally
            if (subtotal.compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal ratio = personSubtotal.divide(subtotal, 10, RoundingMode.HALF_UP);
                if (tax != null) {
                    person.setTaxShare(tax.multiply(ratio).setScale(2, RoundingMode.HALF_UP));
                    person.setTotalOwed(personSubtotal.add(person.getTaxShare()));
                }
                if (serviceCharge != null) {
                    person.setServiceChargeShare(serviceCharge.multiply(ratio).setScale(2, RoundingMode.HALF_UP));
                    person.setTotalOwed(personSubtotal.add(person.getServiceChargeShare()));
                }
                if (discount != null) {
                    person.setDiscountShare(discount.multiply(ratio).setScale(2, RoundingMode.HALF_UP));
                    person.setTotalOwed(personSubtotal.subtract(person.getDiscountShare()));
                }
            } else {
                person.setTaxShare(BigDecimal.ZERO);
                person.setServiceChargeShare(BigDecimal.ZERO);
                person.setDiscountShare(BigDecimal.ZERO);
            }
        }
    }
}