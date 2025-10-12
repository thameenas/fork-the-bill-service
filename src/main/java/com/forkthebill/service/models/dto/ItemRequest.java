package com.forkthebill.service.models.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemRequest {

    private String id;

    @NotBlank(message = "Item name is required")
    private String name;
    
    @NotNull(message = "Item price is required")
    @Positive(message = "Item price must be positive")
    private BigDecimal price;

    private Integer quantity;

    private Integer totalQuantity;
}