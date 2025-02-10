package com.banktest.loanapi.mapper;

import com.banktest.loanapi.dto.CustomerDTO;
import com.banktest.loanapi.model.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerDTO toCustomerDTO(Customer customer);
    Customer toCustomer(CustomerDTO customerDTO);
}
