package com.forkthebill.service.models.dto;

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
public class PersonResponse {
    private Long id;
    private String name;
    
    @Builder.Default
    private List<String> itemsClaimed = new ArrayList<>();
    
    private BigDecimal amountOwed;
    private BigDecimal subtotal;
    private BigDecimal taxShare;
    private BigDecimal tipShare;
    private BigDecimal totalOwed;
    private boolean isFinished;
}