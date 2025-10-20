package com.forkthebill.service.models.entities;

import com.forkthebill.service.exceptions.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ExpenseTest {

    private Expense expense;
    private Item item1;
    private Item item2;
    private Person person1;
    private Person person2;

    @BeforeEach
    void setUp() {
        expense = Expense.builder()
                .id("expense-1")
                .slug("test-expense")
                .createdAt(LocalDateTime.now())
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("10.00"))
                .items(new ArrayList<>())
                .people(new ArrayList<>())
                .build();

        item1 = Item.builder()
                .id("item-1")
                .name("Pizza")
                .price(new BigDecimal("50.00"))
                .claimedBy(new ArrayList<>())
                .build();

        item2 = Item.builder()
                .id("item-2")
                .name("Burger")
                .price(new BigDecimal("30.00"))
                .claimedBy(new ArrayList<>())
                .build();

        person1 = Person.builder()
                .id(UUID.randomUUID())
                .name("Alice")
                .itemsClaimed(new ArrayList<>())
                .amountOwed(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .taxShare(BigDecimal.ZERO)
                .serviceChargeShare(BigDecimal.ZERO)
                .totalOwed(BigDecimal.ZERO)
                .isFinished(false)
                .build();

        person2 = Person.builder()
                .id(UUID.randomUUID())
                .name("Bob")
                .itemsClaimed(new ArrayList<>())
                .amountOwed(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .taxShare(BigDecimal.ZERO)
                .serviceChargeShare(BigDecimal.ZERO)
                .totalOwed(BigDecimal.ZERO)
                .isFinished(false)
                .build();
    }

    @Test
    void addItem_ShouldAddItemAndSetExpenseReference() {
        // When
        expense.addItem(item1);

        // Then
        assertTrue(expense.getItems().contains(item1));
        assertEquals(expense, item1.getExpense());
        assertEquals(1, expense.getItems().size());
    }

    @Test
    void addItem_ShouldAddMultipleItems() {
        // When
        expense.addItem(item1);
        expense.addItem(item2);

        // Then
        assertEquals(2, expense.getItems().size());
        assertTrue(expense.getItems().contains(item1));
        assertTrue(expense.getItems().contains(item2));
        assertEquals(expense, item1.getExpense());
        assertEquals(expense, item2.getExpense());
    }

    @Test
    void removeItem_ShouldRemoveItemAndClearExpenseReference() {
        // Given
        expense.addItem(item1);
        expense.addItem(item2);

        // When
        expense.removeItem(item1);

        // Then
        assertEquals(1, expense.getItems().size());
        assertFalse(expense.getItems().contains(item1));
        assertTrue(expense.getItems().contains(item2));
        assertNull(item1.getExpense());
        assertEquals(expense, item2.getExpense());
    }

    @Test
    void removeItem_ShouldHandleRemovingNonExistentItem() {
        // Given
        expense.addItem(item1);
        Item nonExistentItem = Item.builder()
                .id("non-existent")
                .name("Non-existent")
                .price(BigDecimal.ZERO)
                .claimedBy(new ArrayList<>())
                .build();

        // When
        expense.removeItem(nonExistentItem);

        // Then
        assertEquals(1, expense.getItems().size());
        assertTrue(expense.getItems().contains(item1));
    }

    @Test
    void addPerson_ShouldAddPersonAndSetExpenseReference() {
        // When
        expense.addPerson(person1);

        // Then
        assertTrue(expense.getPeople().contains(person1));
        assertEquals(expense, person1.getExpense());
        assertEquals(1, expense.getPeople().size());
    }

    @Test
    void addPerson_ShouldAddMultiplePeople() {
        // When
        expense.addPerson(person1);
        expense.addPerson(person2);

        // Then
        assertEquals(2, expense.getPeople().size());
        assertTrue(expense.getPeople().contains(person1));
        assertTrue(expense.getPeople().contains(person2));
        assertEquals(expense, person1.getExpense());
        assertEquals(expense, person2.getExpense());
    }

    @Test
    void removePerson_ShouldRemovePersonAndClearExpenseReference() {
        // Given
        expense.addPerson(person1);
        expense.addPerson(person2);

        // When
        expense.removePerson(person1);

        // Then
        assertEquals(1, expense.getPeople().size());
        assertFalse(expense.getPeople().contains(person1));
        assertTrue(expense.getPeople().contains(person2));
        assertNull(person1.getExpense());
        assertEquals(expense, person2.getExpense());
    }

    @Test
    void removePerson_ShouldHandleRemovingNonExistentPerson() {
        // Given
        expense.addPerson(person1);
        Person nonExistentPerson = Person.builder()
                .id(UUID.randomUUID())
                .name("Non-existent")
                .itemsClaimed(new ArrayList<>())
                .amountOwed(BigDecimal.ZERO)
                .subtotal(BigDecimal.ZERO)
                .taxShare(BigDecimal.ZERO)
                .serviceChargeShare(BigDecimal.ZERO)
                .totalOwed(BigDecimal.ZERO)
                .isFinished(false)
                .build();

        // When
        expense.removePerson(nonExistentPerson);

        // Then
        assertEquals(1, expense.getPeople().size());
        assertTrue(expense.getPeople().contains(person1));
    }

    @Test
    void findPersonById_ShouldReturnPerson_WhenPersonExists() {
        // Given
        expense.addPerson(person1);
        expense.addPerson(person2);

        // When
        Person foundPerson = expense.findPersonById(person1.getId());

        // Then
        assertEquals(person1, foundPerson);
    }

    @Test
    void findPersonById_ShouldThrowException_WhenPersonDoesNotExist() {
        // Given
        expense.addPerson(person1);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            expense.findPersonById(UUID.randomUUID());
        });
    }

    @Test
    void findItemById_ShouldReturnItem_WhenItemExists() {
        // Given
        expense.addItem(item1);
        expense.addItem(item2);

        // When
        Item foundItem = expense.findItemById("item-1");

        // Then
        assertEquals(item1, foundItem);
    }

    @Test
    void findItemById_ShouldThrowException_WhenItemDoesNotExist() {
        // Given
        expense.addItem(item1);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            expense.findItemById("non-existent");
        });
    }

    @Test
    void claimItem_ShouldAddPersonToItemClaimedByList() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);

        // When
        expense.claimItem("item-1", person1.getId());

        // Then
        assertTrue(item1.getClaimedBy().contains(person1.getId()));
        assertTrue(person1.getItemsClaimed().contains("item-1"));
    }

    @Test
    void claimItem_ShouldNotDuplicateClaim_WhenPersonAlreadyClaimed() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);
        expense.claimItem("item-1", person1.getId());

        // When
        expense.claimItem("item-1", person1.getId());

        // Then
        assertEquals(1, item1.getClaimedBy().size());
        assertEquals(1, person1.getItemsClaimed().size());
    }

    @Test
    void claimItem_ShouldHandleMultiplePeopleClaimingSameItem() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);
        expense.addPerson(person2);

        // When
        expense.claimItem("item-1", person1.getId());
        expense.claimItem("item-1", person2.getId());

        // Then
        assertEquals(2, item1.getClaimedBy().size());
        assertTrue(item1.getClaimedBy().contains(person1.getId()));
        assertTrue(item1.getClaimedBy().contains(person2.getId()));
        assertTrue(person1.getItemsClaimed().contains("item-1"));
        assertTrue(person2.getItemsClaimed().contains("item-1"));
    }

    @Test
    void unclaimItem_ShouldRemovePersonFromItemClaimedByList() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);
        expense.claimItem("item-1", person1.getId());

        // When
        expense.unclaimItem("item-1", person1.getId());

        // Then
        assertFalse(item1.getClaimedBy().contains(person1.getId()));
        assertFalse(person1.getItemsClaimed().contains("item-1"));
    }

    @Test
    void unclaimItem_ShouldHandleUnclaimingNonClaimedItem() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);

        // When
        expense.unclaimItem("item-1", person1.getId());

        // Then
        assertFalse(item1.getClaimedBy().contains(person1.getId()));
        assertFalse(person1.getItemsClaimed().contains("item-1"));
    }

    @Test
    void claimItem_ShouldRecalculateAmounts_WhenItemIsClaimed() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);

        // When
        expense.claimItem("item-1", person1.getId());

        // Then
        assertTrue(person1.getSubtotal().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(person1.getTotalOwed().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void unclaimItem_ShouldRecalculateAmounts_WhenItemIsUnclaimed() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);
        expense.claimItem("item-1", person1.getId());

        // When
        expense.unclaimItem("item-1", person1.getId());

        // Then
        assertEquals(0, person1.getSubtotal().compareTo(BigDecimal.ZERO));
        assertEquals(0, person1.getTotalOwed().compareTo(BigDecimal.ZERO));
    }

    @Test
    void recalculateAmounts_ShouldCalculateCorrectShares_WhenMultiplePeopleClaimSameItem() {
        // Given
        expense.addItem(item1); // $50.00 item
        expense.addPerson(person1);
        expense.addPerson(person2);

        // When - both people claim the same $50 item
        expense.claimItem("item-1", person1.getId());
        expense.claimItem("item-1", person2.getId());

        // Then - each person should owe $25.00 for the item
        assertEquals(new BigDecimal("25.00"), person1.getSubtotal());
        assertEquals(new BigDecimal("25.00"), person2.getSubtotal());
        
        // Tax and tip should be split proportionally
        assertTrue(person1.getTaxShare().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(person2.getTaxShare().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(person1.getServiceChargeShare().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(person2.getServiceChargeShare().compareTo(BigDecimal.ZERO) > 0);
    }

    @Test
    void recalculateAmounts_ShouldHandleZeroSubtotal() {
        // Given
        expense.setSubtotal(BigDecimal.ZERO);
        expense.addItem(item1);
        expense.addPerson(person1);
        expense.claimItem("item-1", person1.getId());

        // When
        expense.recalculateAmounts();

        // Then
        assertEquals(0, person1.getTaxShare().compareTo(BigDecimal.ZERO));
        assertEquals(0, person1.getServiceChargeShare().compareTo(BigDecimal.ZERO));
    }

    @Test
    void recalculateAmounts_ShouldCalculateTotalOwedCorrectly() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);
        expense.claimItem("item-1", person1.getId());

        // When
        expense.recalculateAmounts();

        // Then
        BigDecimal expectedTotal = person1.getSubtotal()
                .add(person1.getTaxShare())
                .add(person1.getServiceChargeShare());
        assertEquals(expectedTotal, person1.getTotalOwed());
    }

    @Test
    void claimItem_ShouldThrowException_WhenPersonNotFound() {
        // Given
        expense.addItem(item1);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            expense.claimItem("item-1", UUID.randomUUID());
        });
    }

    @Test
    void claimItem_ShouldThrowException_WhenItemNotFound() {
        // Given
        expense.addPerson(person1);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            expense.claimItem("non-existent", person1.getId());
        });
    }

    @Test
    void unclaimItem_ShouldThrowException_WhenPersonNotFound() {
        // Given
        expense.addItem(item1);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            expense.unclaimItem("item-1", UUID.randomUUID());
        });
    }

    @Test
    void unclaimItem_ShouldThrowException_WhenItemNotFound() {
        // Given
        expense.addPerson(person1);

        // When & Then
        assertThrows(ResourceNotFoundException.class, () -> {
            expense.unclaimItem("non-existent", person1.getId());
        });
    }

    @Test
    void calculatePersonSubtotal_ShouldHandleItemWithNoClaims() {
        // Given
        expense.addItem(item1);
        expense.addPerson(person1);
        // Don't claim the item

        // When
        expense.recalculateAmounts();

        // Then
        assertEquals(0, person1.getSubtotal().compareTo(BigDecimal.ZERO));
    }

    @Test
    void calculatePersonSubtotal_ShouldHandleMultipleItemsWithDifferentClaimCounts() {
        // Given
        expense.addItem(item1); // $50.00 - claimed by 2 people
        expense.addItem(item2); // $30.00 - claimed by 1 person
        expense.addPerson(person1);
        expense.addPerson(person2);

        // When
        expense.claimItem("item-1", person1.getId()); // person1 claims item1
        expense.claimItem("item-1", person2.getId()); // person2 claims item1
        expense.claimItem("item-2", person1.getId()); // person1 claims item2

        // Then
        // person1: $25.00 (half of item1) + $30.00 (all of item2) = $55.00
        assertEquals(new BigDecimal("55.00"), person1.getSubtotal());
        // person2: $25.00 (half of item1) = $25.00
        assertEquals(new BigDecimal("25.00"), person2.getSubtotal());
    }
}
