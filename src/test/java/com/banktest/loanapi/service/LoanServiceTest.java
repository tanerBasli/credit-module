package com.banktest.loanapi.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.banktest.loanapi.dto.LoanDTO;
import com.banktest.loanapi.dto.LoanInstallmentDTO;
import com.banktest.loanapi.dto.PaymentResultResponseDTO;
import com.banktest.loanapi.mapper.CustomerMapper;
import com.banktest.loanapi.mapper.LoanInstallmentMapper;
import com.banktest.loanapi.mapper.LoanMapper;
import com.banktest.loanapi.model.Customer;
import com.banktest.loanapi.model.Loan;
import com.banktest.loanapi.model.LoanInstallment;
import com.banktest.loanapi.repository.CustomerRepository;
import com.banktest.loanapi.repository.LoanInstallmentRepository;
import com.banktest.loanapi.repository.LoanRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private LoanRepository loanRepository;

    @Mock
    private LoanInstallmentRepository loanInstallmentRepository;

    @Mock
    private CustomerMapper customerMapper;

    @Mock
    private LoanMapper loanMapper;

    @Mock
    private LoanInstallmentMapper loanInstallmentMapper;

    @InjectMocks
    private LoanService loanService;

    private Customer customer;
    private Loan loan;
    private LoanDTO loanDTO;

    @BeforeEach
    void setUp() {
        customer = new Customer();
        customer.setId(1L);
        customer.setName("John");
        customer.setSurname("Doe");
        customer.setCreditLimit(10000.0);
        customer.setUsedCreditLimit(0.0);

        loan = new Loan();
        loan.setId(1L);
        loan.setCustomerId(1L);
        loan.setLoanAmount(1200.0);
        loan.setNumberOfInstallments(12);
        loan.setCreateDate(LocalDate.now());
        loan.setIsPaid(false);

        loanDTO = new LoanDTO();
        loanDTO.setId(1L);
        loanDTO.setCustomerId(1L);
        loanDTO.setLoanAmount(1200.0);
        loanDTO.setNumberOfInstallments(12);
        loanDTO.setCreateDate(LocalDate.now());
        loanDTO.setIsPaid(false);
    }

    @Test
    void testCreateLoan_Success() {
        // Mock customer repository
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanRepository.save(any(Loan.class))).thenReturn(loan);
        when(loanMapper.toLoanDTO(any(Loan.class))).thenReturn(loanDTO);

        // Call the service method
        LoanDTO result = loanService.createLoan(1L, 1000.0, 0.2, 12);

        // Verify the result
        assertNotNull(result);
        assertEquals(1L, result.getCustomerId());
        assertEquals(1200.0, result.getLoanAmount());
        assertEquals(12, result.getNumberOfInstallments());

        // Verify interactions
        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, times(1)).save(any(Loan.class));
        verify(loanMapper, times(1)).toLoanDTO(any(Loan.class));
    }

    @Test
    void testCreateLoan_CustomerNotFound() {
        // Mock customer repository to return empty
        when(customerRepository.findById(1L)).thenReturn(Optional.empty());

        // Verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.2, 12);
        });

        // Verify interactions
        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testCreateLoan_InsufficientCreditLimit() {
        // Mock customer with insufficient credit limit
        customer.setUsedCreditLimit(9000.0);
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));

        // Verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            loanService.createLoan(1L, 1000.0, 0.2, 12);
        });

        // Verify interactions
        verify(customerRepository, times(1)).findById(1L);
        verify(loanRepository, never()).save(any(Loan.class));
    }

    @Test
    void testListLoans_WithFilters() {
        // Mock data
        Loan loan1 = new Loan();
        loan1.setId(1L);
        loan1.setCustomerId(1L);
        loan1.setLoanAmount(1200.0);
        loan1.setNumberOfInstallments(12);
        loan1.setIsPaid(false);

        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setCustomerId(1L);
        loan2.setLoanAmount(1500.0);
        loan2.setNumberOfInstallments(6);
        loan2.setIsPaid(true);

        LoanDTO loanDTO1 = new LoanDTO();
        loanDTO1.setId(1L);
        loanDTO1.setLoanAmount(1200.0);

        // Mock repository responses
        when(loanRepository.findByCustomerIdAndNumberOfInstallmentsAndIsPaid(1L, 12, false))
                .thenReturn(Arrays.asList(loan1));
        when(loanMapper.toLoanDTO(loan1)).thenReturn(loanDTO1);
        // Call the service method
        List<LoanDTO> result = loanService.listLoans(1L, 12, false);

        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(1200.0, result.get(0).getLoanAmount());

        // Verify interactions
        verify(loanRepository, times(1)).findByCustomerIdAndNumberOfInstallmentsAndIsPaid(1L, 12, false);
    }

    @Test
    void testListLoans_WithoutFilters() {
        // Mock data
        Loan loan1 = new Loan();
        loan1.setId(1L);
        loan1.setCustomerId(1L);
        loan1.setLoanAmount(1200.0);
        loan1.setNumberOfInstallments(12);
        loan1.setIsPaid(false);

        Loan loan2 = new Loan();
        loan2.setId(2L);
        loan2.setCustomerId(1L);
        loan2.setLoanAmount(1500.0);
        loan2.setNumberOfInstallments(6);
        loan2.setIsPaid(true);

        // Mock repository response
        when(loanRepository.findByCustomerId(1L)).thenReturn(Arrays.asList(loan1, loan2));

        // Call the service method
        List<LoanDTO> result = loanService.listLoans(1L, null, null);

        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());

        // Verify interactions
        verify(loanRepository, times(1)).findByCustomerId(1L);
    }

    @Test
    void testListInstallments() {
        // Mock data
        LoanInstallment installment1 = new LoanInstallment();
        installment1.setId(1L);
        installment1.setLoanId(1L);
        installment1.setAmount(100.0);
        installment1.setPaidAmount(0.0);
        installment1.setDueDate(LocalDate.now().plusMonths(1));
        installment1.setPaymentDate(null);
        installment1.setIsPaid(false);

        LoanInstallment installment2 = new LoanInstallment();
        installment2.setId(2L);
        installment2.setLoanId(1L);
        installment2.setAmount(100.0);
        installment2.setPaidAmount(0.0);
        installment2.setDueDate(LocalDate.now().plusMonths(2));
        installment2.setPaymentDate(null);
        installment2.setIsPaid(false);

        // Mock DTOs
        LoanInstallmentDTO installmentDTO1 = new LoanInstallmentDTO();
        installmentDTO1.setId(1L);
        installmentDTO1.setAmount(100.0);
        installmentDTO1.setIsPaid(false);

        LoanInstallmentDTO installmentDTO2 = new LoanInstallmentDTO();
        installmentDTO2.setId(2L);
        installmentDTO2.setAmount(100.0);
        installmentDTO2.setIsPaid(false);

        // Mock repository response
        when(loanInstallmentRepository.findByLoanId(1L))
                .thenReturn(Arrays.asList(installment1, installment2));

        // Mock mapper response
        when(loanInstallmentMapper.toLoanInstallmentDTO(installment1)).thenReturn(installmentDTO1);
        when(loanInstallmentMapper.toLoanInstallmentDTO(installment2)).thenReturn(installmentDTO2);

        // Call the service method
        List<LoanInstallmentDTO> result = loanService.listInstallments(1L);

        // Verify the result
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getId());
        assertEquals(2L, result.get(1).getId());

        // Verify interactions
        verify(loanInstallmentRepository, times(1)).findByLoanId(1L);
        verify(loanInstallmentMapper, times(1)).toLoanInstallmentDTO(installment1);
        verify(loanInstallmentMapper, times(1)).toLoanInstallmentDTO(installment2);
    }

    @Test
    void testPayInstallments_Success() {
        // Mock loan
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomerId(1L);
        loan.setLoanAmount(1200.0);
        loan.setNumberOfInstallments(12);
        loan.setIsPaid(false);

        // Mock customer
        Customer customer = new Customer();
        customer.setId(1L);
        customer.setUsedCreditLimit(500.0); // Example initial credit usage

        // Mock loan installments
        LoanInstallment installment1 = new LoanInstallment();
        installment1.setId(1L);
        installment1.setLoanId(1L);
        installment1.setAmount(100.0);
        installment1.setPaidAmount(0.0);
        installment1.setDueDate(LocalDate.now().plusMonths(1));
        installment1.setPaymentDate(null);
        installment1.setIsPaid(false);

        LoanInstallment installment2 = new LoanInstallment();
        installment2.setId(2L);
        installment2.setLoanId(1L);
        installment2.setAmount(100.0);
        installment2.setPaidAmount(0.0);
        installment2.setDueDate(LocalDate.now().plusMonths(2));
        installment2.setPaymentDate(null);
        installment2.setIsPaid(false);

        // Mock repository responses
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));
        when(loanInstallmentRepository.findByLoanIdAndIsPaidFalseAndDueDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(Arrays.asList(installment1, installment2));
        when(customerRepository.findById(1L)).thenReturn(Optional.of(customer));
        when(loanInstallmentRepository.findByLoanIdAndIsPaidFalse(1L)).thenReturn(Arrays.asList(installment2)); // Loan is not fully paid

        // Call the service method
        PaymentResultResponseDTO result = loanService.payInstallments(1L, 150.0);

        // Verify the result
        assertNotNull(result);
        assertEquals(1, result.getInstallmentsPaid());
        assertEquals(100.0, result.getTotalAmountSpent());
        assertFalse(result.isLoanPaid());

        // Verify interactions
        verify(loanRepository, times(1)).findById(1L);
        verify(loanInstallmentRepository, times(1)).findByLoanIdAndIsPaidFalseAndDueDateBetween(eq(1L), any(LocalDate.class), any(LocalDate.class));
        verify(loanInstallmentRepository, times(1)).save(any(LoanInstallment.class));
        verify(customerRepository, times(1)).findById(1L);
        verify(customerRepository, times(1)).save(any(Customer.class));
    }

    @Test
    void testGetLoanById_Success() {
        // Mock data
        Loan loan = new Loan();
        loan.setId(1L);
        loan.setCustomerId(1L);
        loan.setLoanAmount(1200.0);
        loan.setNumberOfInstallments(12);
        loan.setIsPaid(false);

        // Mock repository response
        when(loanRepository.findById(1L)).thenReturn(Optional.of(loan));

        // Call the service method
        Loan result = loanService.getLoanById(1L);

        // Verify the result
        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getCustomerId());
        assertEquals(1200.0, result.getLoanAmount());

        // Verify interactions
        verify(loanRepository, times(1)).findById(1L);
    }

    @Test
    void testGetLoanById_NotFound() {
        // Mock repository response
        when(loanRepository.findById(1L)).thenReturn(Optional.empty());

        // Verify exception is thrown
        assertThrows(RuntimeException.class, () -> {
            loanService.getLoanById(1L);
        });

        // Verify interactions
        verify(loanRepository, times(1)).findById(1L);
    }
}