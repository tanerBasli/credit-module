package com.banktest.loanapi.dto;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentResultResponseDTO {
    private int installmentsPaid;
    private double totalAmountSpent;
    private boolean isLoanPaid;
}
