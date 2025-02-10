package com.banktest.loanapi.service;

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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class LoanService {

    private final CustomerRepository customerRepository;


    private final LoanRepository loanRepository;


    private final LoanInstallmentRepository loanInstallmentRepository;

    private final CustomerMapper customerMapper;

    private final LoanMapper loanMapper;

    private final LoanInstallmentMapper loanInstallmentMapper;

    public LoanDTO createLoan(Long customerId, Double loanAmount, Double interestRate, Integer numberOfInstallments) {
        // Check if the customer exists
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        // Check if the customer has enough credit limit
        Double totalLoanAmount = loanAmount * (1 + interestRate);
        if (customer.getUsedCreditLimit() + totalLoanAmount > customer.getCreditLimit()) {
            throw new RuntimeException("Customer does not have enough credit limit");
        }

        // Validate number of installments
        List<Integer> validInstallments = Arrays.asList(6, 9, 12, 24);
        if (!validInstallments.contains(numberOfInstallments)) {
            throw new RuntimeException("Number of installments must be 6, 9, 12, or 24");
        }

        // Validate interest rate
        if (interestRate < 0.1 || interestRate > 0.5) {
            throw new RuntimeException("Interest rate must be between 0.1 and 0.5");
        }

        // Create the loan
        Loan loan = new Loan();
        loan.setCustomerId(customerId);
        loan.setLoanAmount(totalLoanAmount);
        loan.setNumberOfInstallments(numberOfInstallments);
        loan.setCreateDate(LocalDate.now());
        loan.setIsPaid(false);

        // Calculate installment amount
        Double installmentAmount = totalLoanAmount / numberOfInstallments;

        // Create installments
        List<LoanInstallment> installments = new ArrayList<>();
        LocalDate dueDate = LocalDate.now().plusMonths(1).withDayOfMonth(1);
        for (int i = 0; i < numberOfInstallments; i++) {
            LoanInstallment installment = new LoanInstallment();
            installment.setLoanId(loan.getId());
            installment.setAmount(installmentAmount);
            installment.setPaidAmount(0.0);
            installment.setDueDate(dueDate.plusMonths(i));
            installment.setPaymentDate(null);
            installment.setIsPaid(false);
            installments.add(installment);
        }

        loan.setInstallments(installments);

        // Update customer's used credit limit
        customer.setUsedCreditLimit(customer.getUsedCreditLimit() + totalLoanAmount);
        customerRepository.save(customer);

        // Save the loan and installments
        loanRepository.save(loan);
        loanInstallmentRepository.saveAll(installments);

        // Convert to DTO and return
        return loanMapper.toLoanDTO(loan);
    }

    public List<LoanDTO> listLoans(Long customerId, Integer numberOfInstallments, Boolean isPaid) {
        // Fetch loans based on customer ID and optional filters
        List<Loan> loans;
        if (numberOfInstallments != null && isPaid != null) {
            loans = loanRepository.findByCustomerIdAndNumberOfInstallmentsAndIsPaid(customerId, numberOfInstallments, isPaid);
        } else if (numberOfInstallments != null) {
            loans = loanRepository.findByCustomerIdAndNumberOfInstallments(customerId, numberOfInstallments);
        } else if (isPaid != null) {
            loans = loanRepository.findByCustomerIdAndIsPaid(customerId, isPaid);
        } else {
            loans = loanRepository.findByCustomerId(customerId);
        }

        // Convert to DTOs and return
        return loans.stream()
                .map(loanMapper::toLoanDTO)
                .collect(Collectors.toList());
    }

    public List<LoanInstallmentDTO> listInstallments(Long loanId) {
        // Fetch installments for the given loan ID
        List<LoanInstallment> installments = loanInstallmentRepository.findByLoanId(loanId);

        // Convert to DTOs and return
        return installments.stream()
                .map(loanInstallmentMapper::toLoanInstallmentDTO)
                .collect(Collectors.toList());
    }

    public PaymentResultResponseDTO payInstallments(Long loanId, Double paymentAmount) {
        // Fetch the loan
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new RuntimeException("Loan not found"));

        // Fetch unpaid installments due within the next 3 months
        LocalDate currentDate = LocalDate.now();
        LocalDate maxDueDate = currentDate.plusMonths(3);
        List<LoanInstallment> unpaidInstallments = loanInstallmentRepository
                .findByLoanIdAndIsPaidFalseAndDueDateBetween(loanId, currentDate, maxDueDate);

        // Sort installments by due date (earliest first)
        unpaidInstallments.sort(Comparator.comparing(LoanInstallment::getDueDate));

        // Calculate how many installments can be paid
        int installmentsPaid = 0;
        double totalAmountSpent = 0.0;

        for (LoanInstallment installment : unpaidInstallments) {
            if (paymentAmount >= installment.getAmount()) {
                double installmentAmount = installment.getAmount();
                long daysDifference = ChronoUnit.DAYS.between(currentDate, installment.getDueDate());

                if (daysDifference > 0) {
                    // Paid before due date: apply discount
                    double discount = installmentAmount * 0.001 * daysDifference;
                    installmentAmount -= discount;
                } else if (daysDifference < 0) {
                    // Paid after due date: apply penalty
                    double penalty = installmentAmount * 0.001 * Math.abs(daysDifference);
                    installmentAmount += penalty;
                }

                // Pay the installment
                installment.setPaidAmount(installment.getAmount());
                installment.setPaymentDate(currentDate);
                installment.setIsPaid(true);
                paymentAmount -= installment.getAmount();
                totalAmountSpent += installment.getAmount();
                installmentsPaid++;

                // Save the updated installment
                loanInstallmentRepository.save(installment);
            } else {
                break; // Not enough money to pay the next installment
            }
        }

        // Update the loan status if all installments are paid
        boolean isLoanPaid = loanInstallmentRepository.findByLoanIdAndIsPaidFalse(loanId).isEmpty();
        if (isLoanPaid) {
            loan.setIsPaid(true);
            loanRepository.save(loan);
        }

        // Update the customer's used credit limit
        Customer customer = customerRepository.findById(loan.getCustomerId())
                .orElseThrow(() -> new RuntimeException("Customer not found"));
        customer.setUsedCreditLimit(customer.getUsedCreditLimit() - totalAmountSpent);
        customerRepository.save(customer);

        // Return the payment result
        return new PaymentResultResponseDTO(installmentsPaid, totalAmountSpent, isLoanPaid);
    }


    public Loan getLoanById(Long loanId) {
      return   loanRepository.findById(loanId).orElseThrow(
              () -> new RuntimeException("Loan not found")
      );
    }
}
