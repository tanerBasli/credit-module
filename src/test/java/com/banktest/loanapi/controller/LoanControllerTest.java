package com.banktest.loanapi.controller;

import com.banktest.loanapi.dto.LoanDTO;
import com.banktest.loanapi.dto.LoanInstallmentDTO;
import com.banktest.loanapi.dto.PaymentResultResponseDTO;
import com.banktest.loanapi.model.Loan;
import com.banktest.loanapi.repository.LoanRepository;
import com.banktest.loanapi.repository.UserRepository;
import com.banktest.loanapi.service.LoanService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
class LoanControllerTest {

    @Mock
    private LoanRepository loanRepository;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private LoanService loanService;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private  UserRepository userRepository;

    @BeforeEach
    public void clearUser(){
        userRepository.deleteAll();
    }


    @Test
    void testCreateLoan_Success() throws Exception {
        // Register and authenticate user
        String token = registerAndObtainAccessToken("testuser@example.com", "password", "Test User");

        // Mock service response
        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setId(1L);
        loanDTO.setCustomerId(1L);
        loanDTO.setLoanAmount(1200.0);
        loanDTO.setNumberOfInstallments(12);
        loanDTO.setCreateDate(LocalDate.now());
        loanDTO.setIsPaid(false);

        when(loanService.createLoan(1L, 1000.0, 0.2, 12)).thenReturn(loanDTO);

        // Perform the request
        mockMvc.perform(post("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .param("customerId", "1")
                        .param("loanAmount", "1000.0")
                        .param("interestRate", "0.2")
                        .param("numberOfInstallments", "12")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1L))
                .andExpect(jsonPath("$.customerId").value(1L))
                .andExpect(jsonPath("$.loanAmount").value(1200.0))
                .andExpect(jsonPath("$.numberOfInstallments").value(12));

        // Verify service call
        verify(loanService, times(1)).createLoan(1L, 1000.0, 0.2, 12);
    }

    @Test
    void testListLoans_WithFilters() throws Exception {
        // Mock data
        LoanDTO loanDTO = new LoanDTO();
        loanDTO.setId(1L);
        loanDTO.setCustomerId(1L);
        loanDTO.setLoanAmount(1200.0);
        loanDTO.setNumberOfInstallments(12);
        loanDTO.setIsPaid(false);

        // Register and authenticate user
        String token = registerAndObtainAccessToken("testuser@example.com", "password", "Test User");

        // Mock service response
        when(loanService.listLoans(1L, 12, false)).thenReturn(Arrays.asList(loanDTO));

        // Perform the request
        mockMvc.perform(get("/api/loans")
                        .header("Authorization", "Bearer " + token)
                        .param("customerId", "1")
                        .param("numberOfInstallments", "12")
                        .param("isPaid", "false")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].customerId").value(1L))
                .andExpect(jsonPath("$[0].loanAmount").value(1200.0))
                .andExpect(jsonPath("$[0].numberOfInstallments").value(12))
                .andExpect(jsonPath("$[0].isPaid").value(false));

        // Verify service call
        verify(loanService, times(1)).listLoans(1L, 12, false);
    }

    private String registerAndObtainAccessToken(String email, String password, String fullName) throws Exception {
        // Register the test user
        mockMvc.perform(post("/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\", \"password\":\"" + password + "\", \"fullName\":\"" + fullName + "\"}"))
                .andExpect(status().isOk());

        // Now login and get the token
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\", \"password\":\"" + password + "\"}"))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return new ObjectMapper().readTree(responseBody).get("token").asText();
    }

    @Test
    void testListInstallments() throws Exception {
        // Mock data
        LoanInstallmentDTO installmentDTO1 = new LoanInstallmentDTO();
        installmentDTO1.setId(1L);
        installmentDTO1.setLoanId(1L);
        installmentDTO1.setAmount(100.0);
        installmentDTO1.setPaidAmount(0.0);
        installmentDTO1.setDueDate(LocalDate.now().plusMonths(1));
        installmentDTO1.setPaymentDate(null);
        installmentDTO1.setIsPaid(false);

        LoanInstallmentDTO installmentDTO2 = new LoanInstallmentDTO();
        installmentDTO2.setId(2L);
        installmentDTO2.setLoanId(1L);
        installmentDTO2.setAmount(100.0);
        installmentDTO2.setPaidAmount(0.0);
        installmentDTO2.setDueDate(LocalDate.now().plusMonths(2));
        installmentDTO2.setPaymentDate(null);
        installmentDTO2.setIsPaid(false);

        Loan loan;
        loan = new Loan();
        loan.setId(1L);
        loan.setCustomerId(1L);
        loan.setLoanAmount(1200.0);
        loan.setNumberOfInstallments(12);
        loan.setCreateDate(LocalDate.now());
        loan.setIsPaid(false);

        // Register and authenticate user
        String token = registerAndObtainAccessToken("testuser@example.com", "password", "Test User");


        // Mock service response
        when(loanService.getLoanById(1L)).thenReturn(loan);
        when(loanService.listInstallments(1L)).thenReturn(Arrays.asList(installmentDTO1, installmentDTO2));

        // Perform the request
        mockMvc.perform(get("/api/loans/1/installments")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1L))
                .andExpect(jsonPath("$[0].loanId").value(1L))
                .andExpect(jsonPath("$[0].amount").value(100.0))
                .andExpect(jsonPath("$[1].id").value(2L))
                .andExpect(jsonPath("$[1].loanId").value(1L))
                .andExpect(jsonPath("$[1].amount").value(100.0));

        // Verify service call
        verify(loanService, times(1)).listInstallments(1L);
    }

    @Test
    void testPayInstallments_Success() throws Exception {
        Loan loan;
        loan = new Loan();
        loan.setId(1L);
        loan.setCustomerId(1L);
        loan.setLoanAmount(1200.0);
        loan.setNumberOfInstallments(12);
        loan.setCreateDate(LocalDate.now());
        loan.setIsPaid(false);
        // Register and authenticate user
        String token = registerAndObtainAccessToken("testuser@example.com", "password", "Test User");
        // Mock service response
        PaymentResultResponseDTO paymentResult = new PaymentResultResponseDTO(1, 100.0, false);
        when(loanService.getLoanById(1L)).thenReturn(loan);
        when(loanService.payInstallments(1L, 150.0)).thenReturn(paymentResult);

        // Perform the request
        mockMvc.perform(post("/api/loans/1/pay")
                        .header("Authorization", "Bearer " + token)
                        .param("paymentAmount", "150.0")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.installmentsPaid").value(1))
                .andExpect(jsonPath("$.totalAmountSpent").value(100.0))
                .andExpect(jsonPath("$.loanPaid").value(false));
        // Verify service call
        verify(loanService, times(1)).payInstallments(1L, 150.0);
    }

}