package com.banktest.loanapi.controller;


import com.banktest.loanapi.dto.LoginResponse;
import com.banktest.loanapi.dto.LoginUserDto;
import com.banktest.loanapi.dto.RegisterResponse;
import com.banktest.loanapi.dto.RegisterUserDto;
import com.banktest.loanapi.model.User;
import com.banktest.loanapi.service.AuthenticationService;
import com.banktest.loanapi.service.JwtService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtService jwtService;

    private final AuthenticationService authenticationService;

    public AuthenticationController(JwtService jwtService, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/signup")
    public ResponseEntity<RegisterResponse> register(@RequestBody RegisterUserDto registerUserDto) {
        User registeredUser = authenticationService.signup(registerUserDto);

        String jwtToken = jwtService.generateToken(registeredUser);

        return ResponseEntity.ok(RegisterResponse.builder().
                email(registeredUser.getEmail()).
                fullName(registeredUser.getFullName()).
                token(jwtToken).
                expiresIn(jwtService.getExpirationTime()).
                build());
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody LoginUserDto loginUserDto) {
        User authenticatedUser = authenticationService.authenticate(loginUserDto);

        String jwtToken = jwtService.generateToken(authenticatedUser);

        return ResponseEntity.ok(LoginResponse.builder().
                token(jwtToken).
                expiresIn(jwtService.getExpirationTime())
                .roles(authenticatedUser.getRoles()) // Add roles here
                .build());
    }
}