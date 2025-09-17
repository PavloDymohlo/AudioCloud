package ua.dymohlo.auth_service.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.auth_service.dto.request.LoginInRequest;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.dto.response.AuthResponse;
import ua.dymohlo.auth_service.dto.response.RegisteredUserInfoResponse;
import ua.dymohlo.auth_service.entiti.User;
import ua.dymohlo.auth_service.security.JwtTokenService;
import ua.dymohlo.auth_service.service.AuthService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "User authentication and registration operations")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/register")
    @Operation(
            summary = "Register new user",
            description = "Creates a new user account with payment processing and subscription assignment. Includes SAGA pattern for transaction management."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully with JWT token"),
            @ApiResponse(responseCode = "400", description = "Invalid input data or user already exists"),
            @ApiResponse(responseCode = "403", description = "Payment failed or subscription assignment error"),
            @ApiResponse(responseCode = "500", description = "Internal server error during registration process")
    })
    public AuthResponse registerUser(@Valid @RequestBody RegisterRequest request) {
        RegisteredUserInfoResponse response = authService.register(request);
        String token = jwtTokenService.generateToken(response.getUser());
        return AuthResponse.builder()
                .token(token)
                .subscriptionName(response.getSubscriptionName())
                .bankCardNumber(request.getBankCardNumber())
                .bankCardNumberCVV(request.getBankCardNumberCVV())
                .bankCardNumberExpired(request.getBankCardNumberExpired())
                .build();
    }


    @PostMapping("/login")
    @Operation(
            summary = "User login",
            description = "Authenticates user credentials and returns JWT access token for API authorization"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, JWT token returned"),
            @ApiResponse(responseCode = "400", description = "Invalid input data format"),
            @ApiResponse(responseCode = "403", description = "Invalid email or password"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error during authentication")
    })
    public String getToken(@Valid @RequestBody LoginInRequest request) {
        User user = authService.loginIn(request);
        return jwtTokenService.generateToken(user);
    }
}
