package ua.dymohlo.auth_service.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ua.dymohlo.auth_service.dto.request.LoginInRequest;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.entiti.User;
import ua.dymohlo.auth_service.security.JwtTokenService;
import ua.dymohlo.auth_service.service.AuthService;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {
    private final AuthService authService;
    private final JwtTokenService jwtTokenService;

    @PostMapping("/register")
    public String registerUser(@Valid @RequestBody RegisterRequest request) {
        User user = authService.register(request);
        return jwtTokenService.generateToken(user);
    }

    @PostMapping("/login")
    public String getToken(@Valid @RequestBody LoginInRequest request) {
        User user = authService.loginIn(request);
        return jwtTokenService.generateToken(user);
    }
}
