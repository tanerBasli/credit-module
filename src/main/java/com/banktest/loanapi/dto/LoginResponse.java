package com.banktest.loanapi.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class LoginResponse {
    private String token;

    private long expiresIn;

    private List<String> roles;

    public String getToken() {
        return token;
    }
}
