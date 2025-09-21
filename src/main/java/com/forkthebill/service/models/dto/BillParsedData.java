package com.forkthebill.service.models.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BillParsedData {
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal tip;
    private BigDecimal totalAmount;
    private List<BillItem> items;
    private String restaurantName;
    private String date;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class BillItem {
        private String name;
        private BigDecimal price;
        private Integer quantity;
    }
}
