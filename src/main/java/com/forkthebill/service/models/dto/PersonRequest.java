package com.forkthebill.service.models.dto;

import jakarta.validation.constraints.NotBlank;
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
public class PersonRequest {
    @NotBlank(message = "Person name is required")
    private String name;
    
    @Builder.Default
    private List<String> itemsClaimed = new ArrayList<>();
    
    private BigDecimal subtotal;
    private BigDecimal taxShare;
    private BigDecimal serviceChargeShare;
    private BigDecimal totalOwed;
    
    @Builder.Default
    private boolean isFinished = false;
}