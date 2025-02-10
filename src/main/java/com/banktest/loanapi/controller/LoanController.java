package com.banktest.loanapi.controller;

import com.banktest.loanapi.config.JwtTokenUtil;
import com.banktest.loanapi.dto.LoanDTO;
import com.banktest.loanapi.dto.LoanInstallmentDTO;
import com.banktest.loanapi.dto.PaymentResultResponseDTO;
import com.banktest.loanapi.model.Loan;
import com.banktest.loanapi.service.LoanService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
public class LoanController {


    private final LoanService loanService;


    private final JwtTokenUtil jwtTokenUtil;



    private Integer getCurrentUserId(HttpServletRequest request) {
        String token = jwtTokenUtil.resolveToken(request);
        return jwtTokenUtil.getUserIdFromToken(token);
    }

    @PostMapping
    public ResponseEntity<LoanDTO> createLoan(@RequestParam Long customerId,
                                              @RequestParam Double loanAmount,
                                              @RequestParam Double interestRate,
                                              @RequestParam Integer numberOfInstallments,
                                              HttpServletRequest request
    ) {
        Integer currentUserId = getCurrentUserId(request);
        if (!jwtTokenUtil.isAdmin(request) && !currentUserId.equals(customerId)) {
            throw new AccessDeniedException("You are not authorized to perform this action");
        }

        LoanDTO loanDTO = loanService.createLoan(customerId, loanAmount, interestRate, numberOfInstallments);
        return ResponseEntity.ok(loanDTO);
    }
    @GetMapping
    public ResponseEntity<List<LoanDTO>> listLoans(
            @RequestParam Long customerId,
            @RequestParam(required = false) Integer numberOfInstallments,
            @RequestParam(required = false) Boolean isPaid,
            HttpServletRequest request) {

        // Check if the current user is an ADMIN or the customer themselves
        Integer currentUserId = getCurrentUserId(request);
        if (!jwtTokenUtil.isAdmin(request) && !currentUserId.equals(customerId)) {
            throw new AccessDeniedException("You are not authorized to perform this action");
        }

        List<LoanDTO> loans = loanService.listLoans(customerId, numberOfInstallments, isPaid);
        return ResponseEntity.ok(loans);
    }

    @GetMapping("/{loanId}/installments")
    public ResponseEntity<List<LoanInstallmentDTO>> listInstallments(
            @PathVariable Long loanId,
            HttpServletRequest request) {

        // Fetch the loan to check the customer ID
        Loan loan = loanService.getLoanById(loanId);
        Long customerId = loan.getCustomerId();

        // Check if the current user is an ADMIN or the customer themselves
        Integer currentUserId = getCurrentUserId(request);
        if (!jwtTokenUtil.isAdmin(request) && !currentUserId.equals(customerId)) {
            throw new AccessDeniedException("You are not authorized to perform this action");
        }

        List<LoanInstallmentDTO> installments = loanService.listInstallments(loanId);
        return ResponseEntity.ok(installments);
    }

    @PostMapping("/{loanId}/pay")
    public ResponseEntity<PaymentResultResponseDTO> payInstallments(
            @PathVariable Long loanId,
            @RequestParam Double paymentAmount,
            HttpServletRequest request) {

        // Fetch the loan to check the customer ID
        Loan loan = loanService.getLoanById(loanId);
        Long customerId = loan.getCustomerId();

        // Check if the current user is an ADMIN or the customer themselves
        Integer currentUserId = getCurrentUserId(request);
        if (!jwtTokenUtil.isAdmin(request) && !currentUserId.equals(customerId)) {
            throw new AccessDeniedException("You are not authorized to perform this action");
        }

        PaymentResultResponseDTO result = loanService.payInstallments(loanId, paymentAmount);
        return ResponseEntity.ok(result);
    }
}

