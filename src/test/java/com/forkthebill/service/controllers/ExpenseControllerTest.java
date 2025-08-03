package com.forkthebill.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkthebill.service.models.dto.ExpenseRequest;
import com.forkthebill.service.models.dto.ExpenseResponse;
import com.forkthebill.service.models.dto.ItemRequest;
import com.forkthebill.service.services.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class ExpenseControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ExpenseService expenseService;

    @InjectMocks
    private ExpenseController expenseController;

    private ObjectMapper objectMapper;

    @BeforeEach
    public void setup() {
        mockMvc = MockMvcBuilders.standaloneSetup(expenseController).build();
        objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules(); // For LocalDateTime serialization
    }

    @Test
    public void createExpense_shouldReturnCreatedExpense() throws Exception {
        // Given
        ExpenseRequest request = createValidExpenseRequest();
        
        ExpenseResponse response = ExpenseResponse.builder()
                .id("1")
                .slug("test-slug")
                .createdAt(LocalDateTime.now())
                .payerName(request.getPayerName())
                .totalAmount(request.getTotalAmount())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .tip(request.getTip())
                .build();
        
        when(expenseService.createExpense(any(ExpenseRequest.class))).thenReturn(response);

        // When & Then
        mockMvc.perform(post("/expense")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.slug").value("test-slug"))
                .andExpect(jsonPath("$.payerName").value(request.getPayerName()))
                .andExpect(jsonPath("$.totalAmount").value(request.getTotalAmount().doubleValue()));
    }
    
    private ExpenseRequest createValidExpenseRequest() {
        return ExpenseRequest.builder()
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .tip(new BigDecimal("10.00"))
                .items(List.of(
                        ItemRequest.builder()
                                .name("Burger")
                                .price(new BigDecimal("80.00"))
                                .build()
                ))
                .build();
    }
}