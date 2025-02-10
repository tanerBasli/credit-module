package com.banktest.loanapi.repository;

import com.banktest.loanapi.model.LoanInstallment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface LoanInstallmentRepository extends JpaRepository<LoanInstallment, Long> {
    List<LoanInstallment> findByLoanId(Long loanId);
    List<LoanInstallment> findByLoanIdAndIsPaidFalseAndDueDateBetween(Long loanId, LocalDate startDate, LocalDate endDate);
    List<LoanInstallment> findByLoanIdAndIsPaidFalse(Long loanId);
}
