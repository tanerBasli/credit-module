package com.banktest.loanapi.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class LoanDTO {
    private Long id;
    private Long customerId;
    private Double loanAmount;
    private Integer numberOfInstallments;
    private LocalDate createDate;
    private Boolean isPaid;
    private List<LoanInstallmentDTO> installments;
}
