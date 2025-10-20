package com.forkthebill.service.models.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseRequest {
    @NotBlank(message = "Payer name is required")
    private String payerName;

    private String restaurantName;

    @NotNull(message = "Total amount is required")
    @Positive(message = "Total amount must be positive")
    private BigDecimal totalAmount;

    @NotNull(message = "Subtotal is required")
    @Positive(message = "Subtotal must be positive")
    private BigDecimal subtotal;

    @NotNull(message = "Tax is required")
    @Positive(message = "Tax must be positive")
    private BigDecimal tax;

    private BigDecimal serviceCharge;

    @NotNull(message = "Items list cannot be null")
    @Size(min = 1, message = "At least one item is required")
    @Valid
    @Builder.Default
    private List<ItemRequest> items = new ArrayList<>();

    @Valid
    @Builder.Default
    private List<PersonRequest> people = new ArrayList<>();
}