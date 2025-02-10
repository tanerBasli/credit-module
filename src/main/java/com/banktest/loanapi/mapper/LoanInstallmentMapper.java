package com.banktest.loanapi.mapper;

import com.banktest.loanapi.dto.LoanInstallmentDTO;
import com.banktest.loanapi.model.LoanInstallment;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface LoanInstallmentMapper {
    LoanInstallmentDTO toLoanInstallmentDTO(LoanInstallment loanInstallment);
    LoanInstallment toLoanInstallment(LoanInstallmentDTO loanInstallmentDTO);
}