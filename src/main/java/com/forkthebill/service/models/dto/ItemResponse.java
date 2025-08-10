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
public class ItemResponse {
    private String id;
    private String name;
    private BigDecimal price;
    
    @Builder.Default
    private List<Long> claimedBy = new ArrayList<>();
}