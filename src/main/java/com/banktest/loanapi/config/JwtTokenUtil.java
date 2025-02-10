package com.banktest.loanapi.config;

import com.banktest.loanapi.model.User;
import com.banktest.loanapi.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class JwtTokenUtil {
    @Value("${security.jwt.secret-key}")
    private String jwtSecret;

    private final UserRepository userRepository;


    // Method to extract the token from the Authorization header
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);  // Extract token from "Bearer <token>"
        }
        return null;
    }

    // Method to extract a claim from the JWT token
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Method to extract all claims from the JWT token
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtSecret)  // Make sure to use the same secret key as used for signing the token
                .parseClaimsJws(token)
                .getBody();
    }

    // Method to get the username (or email) from the JWT token
    public Integer getUserIdFromToken(String token) {
        String   userEmail= getClaimFromToken(token, Claims::getSubject);
       User user= userRepository.findByEmail(userEmail).orElseThrow(() -> new RuntimeException("Customer not found"));
        return user.getId();
    }

    // Method to check if the user is an admin
    public boolean isAdmin(HttpServletRequest request) {
        String token = resolveToken(request);
        if (token == null) {
            return false;  // Return false if the token is missing or invalid
        }
        List<String> roles = getClaimFromToken(token, claims -> claims.get("roles", List.class));
        return roles != null && roles.contains("ROLE_ADMIN");
    }
}