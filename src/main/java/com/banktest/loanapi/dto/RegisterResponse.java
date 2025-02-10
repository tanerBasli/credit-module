package com.banktest.loanapi.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegisterResponse {
    private String token;

    private long expiresIn;

    private String fullName;

    private String email;

}
