package com.forkthebill.service.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.forkthebill.service.exceptions.GlobalExceptionHandler;
import com.forkthebill.service.exceptions.ResourceNotFoundException;
import com.forkthebill.service.exceptions.ValidationException;
import com.forkthebill.service.models.dto.ClaimItemRequest;
import com.forkthebill.service.models.dto.ExpenseRequest;
import com.forkthebill.service.models.dto.ExpenseResponse;
import com.forkthebill.service.models.dto.ItemRequest;
import com.forkthebill.service.models.dto.PersonRequest;
import com.forkthebill.service.models.dto.PersonResponse;
import com.forkthebill.service.services.ExpenseService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
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
        mockMvc = MockMvcBuilders.standaloneSetup(expenseController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
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
                .serviceCharge(request.getServiceCharge())
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
    
    @Test
    public void getExpenseBySlug_shouldReturnExpense_whenExpenseExists() throws Exception {
        // Given
        String slug = "test-slug";
        ExpenseResponse response = ExpenseResponse.builder()
                .id("1")
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("10.00"))
                .build();
        
        when(expenseService.getExpenseBySlug(slug)).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/expense/{slug}", slug))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.payerName").value("John Doe"))
                .andExpect(jsonPath("$.totalAmount").value(100.00));
    }
    
    @Test
    public void getExpenseBySlug_shouldReturn404_whenExpenseDoesNotExist() throws Exception {
        // Given
        String slug = "non-existent-slug";
        when(expenseService.getExpenseBySlug(slug)).thenThrow(new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        // When & Then
        mockMvc.perform(get("/expense/{slug}", slug))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void updateExpenseBySlug_shouldReturnUpdatedExpense_whenExpenseExists() throws Exception {
        // Given
        String slug = "test-slug";
        ExpenseRequest request = createValidExpenseRequest();
        request.setPayerName("Jane Doe"); // Changed from John Doe
        
        ExpenseResponse response = ExpenseResponse.builder()
                .id("1")
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .payerName(request.getPayerName())
                .totalAmount(request.getTotalAmount())
                .subtotal(request.getSubtotal())
                .tax(request.getTax())
                .serviceCharge(request.getServiceCharge())
                .build();
        
        when(expenseService.updateExpenseBySlug(eq(slug), any(ExpenseRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}", slug)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.slug").value(slug))
                .andExpect(jsonPath("$.payerName").value("Jane Doe"))
                .andExpect(jsonPath("$.totalAmount").value(request.getTotalAmount().doubleValue()));
    }
    
    @Test
    public void updateExpenseBySlug_shouldReturn404_whenExpenseDoesNotExist() throws Exception {
        // Given
        String slug = "non-existent-slug";
        ExpenseRequest request = createValidExpenseRequest();
        
        when(expenseService.updateExpenseBySlug(eq(slug), any(ExpenseRequest.class)))
                .thenThrow(new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}", slug)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void updateExpenseBySlug_shouldReturn400_whenValidationFails() throws Exception {
        // Given
        String slug = "test-slug";
        ExpenseRequest request = ExpenseRequest.builder()
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("5.00")) // This makes the total 95, not 100
                .items(List.of(
                        ItemRequest.builder()
                                .name("Burger")
                                .price(new BigDecimal("80.00"))
                                .build()
                ))
                .build();
        
        when(expenseService.updateExpenseBySlug(eq(slug), any(ExpenseRequest.class)))
                .thenThrow(new ValidationException("Total amount must equal subtotal + tax + tip"));
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}", slug)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void claimItem_shouldReturnUpdatedExpense_whenClaimSuccessful() throws Exception {
        // Given
        String slug = "test-slug";
        String itemId = "item-1";
        UUID personId = UUID.randomUUID();
        ClaimItemRequest request = ClaimItemRequest.builder()
                .personId(personId)
                .build();
        
        ExpenseResponse response = ExpenseResponse.builder()
                .id("1")
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("10.00"))
                .build();
        
        when(expenseService.claimItem(eq(slug), eq(itemId), eq(personId))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(post("/expense/{slug}/items/{itemId}/claim", slug, itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.slug").value(slug));
    }
    
    @Test
    public void claimItem_shouldReturn404_whenExpenseNotFound() throws Exception {
        // Given
        String slug = "non-existent-slug";
        String itemId = "item-1";
        UUID personId = UUID.randomUUID();
        ClaimItemRequest request = ClaimItemRequest.builder()
                .personId(personId)
                .build();
        
        when(expenseService.claimItem(eq(slug), eq(itemId), eq(personId)))
                .thenThrow(new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        // When & Then
        mockMvc.perform(post("/expense/{slug}/items/{itemId}/claim", slug, itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void claimItem_shouldReturn400_whenValidationFails() throws Exception {
        // Given
        String slug = "test-slug";
        String itemId = "item-1";
        UUID personId = UUID.randomUUID();
        ClaimItemRequest request = ClaimItemRequest.builder()
                .personId(personId)
                .build();
        
        when(expenseService.claimItem(eq(slug), eq(itemId), eq(personId)))
                .thenThrow(new ValidationException("Item is already claimed by this person"));
        
        // When & Then
        mockMvc.perform(post("/expense/{slug}/items/{itemId}/claim", slug, itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void unclaimItem_shouldReturnUpdatedExpense_whenUnclaimSuccessful() throws Exception {
        // Given
        String slug = "test-slug";
        String itemId = "item-1";
        UUID personId = UUID.randomUUID();

        ExpenseResponse response = ExpenseResponse.builder()
                .id("1")
                .slug(slug)
                .createdAt(LocalDateTime.now())
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("10.00"))
                .build();
        
        when(expenseService.unclaimItem(eq(slug), eq(itemId), eq(personId))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(delete("/expense/{slug}/items/{itemId}/claim/{personId}", slug, itemId, personId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.slug").value(slug));
    }
    
    @Test
    public void unclaimItem_shouldReturn404_whenExpenseNotFound() throws Exception {
        // Given
        String slug = "non-existent-slug";
        String itemId = "item-1";
        UUID personId = UUID.randomUUID();
        
        when(expenseService.unclaimItem(eq(slug), eq(itemId), eq(personId)))
                .thenThrow(new ResourceNotFoundException("Expense not found with slug: " + slug));
        
        // When & Then
        mockMvc.perform(delete("/expense/{slug}/items/{itemId}/claim/{personId}", slug, itemId, personId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void unclaimItem_shouldReturn400_whenValidationFails() throws Exception {
        // Given
        String slug = "test-slug";
        String itemId = "item-1";
        UUID personId = UUID.randomUUID();
        
        when(expenseService.unclaimItem(eq(slug), eq(itemId), eq(personId)))
                .thenThrow(new ValidationException("Item is not claimed by this person"));
        
        // When & Then
        mockMvc.perform(delete("/expense/{slug}/items/{itemId}/claim/{personId}", slug, itemId, personId))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    public void markPersonAsFinished_shouldReturn200_whenSuccessful() throws Exception {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.randomUUID();
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}/people/{personId}/finish", slug, personId))
                .andExpect(status().isOk());
        
        verify(expenseService).markPersonAsFinished(slug, personId);
    }
    
    @Test
    public void markPersonAsFinished_shouldReturn404_whenExpenseNotFound() throws Exception {
        // Given
        String slug = "non-existent-slug";
        UUID personId = UUID.randomUUID();
        
        doThrow(new ResourceNotFoundException("Expense not found with slug: " + slug))
                .when(expenseService).markPersonAsFinished(slug, personId);
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}/people/{personId}/finish", slug, personId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void markPersonAsFinished_shouldReturn404_whenPersonNotFound() throws Exception {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.randomUUID();
        
        doThrow(new ResourceNotFoundException("Person not found with ID: " + personId))
                .when(expenseService).markPersonAsFinished(slug, personId);
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}/people/{personId}/finish", slug, personId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void markPersonAsPending_shouldReturn200_whenSuccessful() throws Exception {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.randomUUID();
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}/people/{personId}/pending", slug, personId))
                .andExpect(status().isOk());
        
        verify(expenseService).markPersonAsPending(slug, personId);
    }
    
    @Test
    public void markPersonAsPending_shouldReturn404_whenExpenseNotFound() throws Exception {
        // Given
        String slug = "non-existent-slug";
        UUID personId = UUID.randomUUID();
        
        doThrow(new ResourceNotFoundException("Expense not found with slug: " + slug))
                .when(expenseService).markPersonAsPending(slug, personId);
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}/people/{personId}/pending", slug, personId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    public void markPersonAsPending_shouldReturn404_whenPersonNotFound() throws Exception {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.randomUUID();
        
        doThrow(new ResourceNotFoundException("Person not found with ID: " + personId))
                .when(expenseService).markPersonAsPending(slug, personId);
        
        // When & Then
        mockMvc.perform(put("/expense/{slug}/people/{personId}/pending", slug, personId))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void markPersonAsPending_ShouldCallServiceAndReturnOk() {
        // Given
        String slug = "test-slug";
        UUID personId = UUID.randomUUID();

        // When
        ResponseEntity<Void> response = expenseController.markPersonAsPending(slug, personId);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(expenseService).markPersonAsPending(slug, personId);
    }

    @Test
    void addPersonToExpense_ShouldCallServiceAndReturnOk() {
        // Given
        String slug = "test-slug";
        PersonRequest personRequest = PersonRequest.builder()
                .name("John Doe")
                .build();

        ExpenseResponse expectedResponse = ExpenseResponse.builder()
                .slug(slug)
                .people(Arrays.asList(
                        PersonResponse.builder()
                                .name("John Doe")
                                .build()
                ))
                .build();

        when(expenseService.addPersonToExpense(slug, personRequest)).thenReturn(expectedResponse);

        // When
        ResponseEntity<ExpenseResponse> response = expenseController.addPersonToExpense(slug, personRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(expenseService).addPersonToExpense(slug, personRequest);
    }

    @Test
    void addPersonToExpense_WithValidRequest_ShouldReturnCorrectResponse() {
        // Given
        String slug = "test-slug";
        PersonRequest personRequest = PersonRequest.builder()
                .name("Jane Smith")
                .isFinished(true)
                .build();

        ExpenseResponse expectedResponse = ExpenseResponse.builder()
                .slug(slug)
                .people(Arrays.asList(
                        PersonResponse.builder()
                                .name("Jane Smith")
                                .isFinished(true)
                                .build()
                ))
                .build();

        when(expenseService.addPersonToExpense(slug, personRequest)).thenReturn(expectedResponse);

        // When
        ResponseEntity<ExpenseResponse> response = expenseController.addPersonToExpense(slug, personRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Jane Smith", response.getBody().getPeople().get(0).getName());
        assertTrue(response.getBody().getPeople().get(0).isFinished());
        verify(expenseService).addPersonToExpense(slug, personRequest);
    }
    
    private ExpenseRequest createValidExpenseRequest() {
        return ExpenseRequest.builder()
                .payerName("John Doe")
                .totalAmount(new BigDecimal("100.00"))
                .subtotal(new BigDecimal("80.00"))
                .tax(new BigDecimal("10.00"))
                .serviceCharge(new BigDecimal("10.00"))
                .items(List.of(
                        ItemRequest.builder()
                                .name("Burger")
                                .price(new BigDecimal("80.00"))
                                .build()
                ))
                .build();
    }
}