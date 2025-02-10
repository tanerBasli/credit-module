package com.banktest.loanapi.mapper;

import com.banktest.loanapi.dto.LoanDTO;
import com.banktest.loanapi.model.Loan;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = {LoanInstallmentMapper.class})
public interface LoanMapper {
    LoanDTO toLoanDTO(Loan loan);
    Loan toLoan(LoanDTO loanDTO);
}