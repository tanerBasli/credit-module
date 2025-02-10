package com.banktest.loanapi.dto;

import lombok.Data;

@Data
public class LoginUserDto {
    private String email;

    private String password;

}