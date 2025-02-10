package com.banktest.loanapi.dto;

import lombok.Data;

@Data
public class CustomerDTO {
    private Long id;
    private String name;
    private String surname;
    private Double creditLimit;
    private Double usedCreditLimit;
}