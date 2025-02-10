package com.banktest.loanapi.dto;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanInstallmentDTO {
    private Long id;
    private Long loanId;
    private Double amount;
    private Double paidAmount;
    private LocalDate dueDate;
    private LocalDate paymentDate;
    private Boolean isPaid;
}
