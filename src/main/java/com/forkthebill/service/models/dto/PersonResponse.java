package com.forkthebill.service.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PersonResponse {
    private UUID id;
    private String name;
    
    @Builder.Default
    private List<String> itemsClaimed = new ArrayList<>();
    
    private BigDecimal amountOwed;
    private BigDecimal subtotal;
    private BigDecimal taxShare;
    private BigDecimal serviceChargeShare;
    private BigDecimal totalOwed;
    private boolean isFinished;
}