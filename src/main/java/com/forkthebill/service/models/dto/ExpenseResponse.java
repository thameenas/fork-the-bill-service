package com.forkthebill.service.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ExpenseResponse {
    private String id;
    private String slug;
    private String restaurantName;
    private LocalDateTime createdAt;
    private String payerName;
    private BigDecimal totalAmount;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal serviceCharge;

    @Builder.Default
    private List<ItemResponse> items = new ArrayList<>();

    @Builder.Default
    private List<PersonResponse> people = new ArrayList<>();
}