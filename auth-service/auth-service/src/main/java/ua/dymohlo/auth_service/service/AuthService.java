package ua.dymohlo.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.dymohlo.auth_service.dto.request.LoginInRequest;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.dto.response.RegisteredUserInfoResponse;
import ua.dymohlo.auth_service.entiti.User;
import ua.dymohlo.auth_service.exception.InvalidCredentialsException;
import ua.dymohlo.auth_service.models.UserRole;
import ua.dymohlo.auth_service.repository.UserRepository;
import ua.dymohlo.auth_service.saga.service.SagaOrchestrator;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SagaOrchestrator sagaOrchestrator;
    private static final UserRole DEFAULT_USER_ROLE = UserRole.CLIENT;

    public RegisteredUserInfoResponse register(RegisterRequest request) {
        return sagaOrchestrator.register(request);
    }

    public User loginIn(LoginInRequest request) {
        return userRepository.findByUserEmail(request.getUserEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getUserPassword()))
                .map(user -> {
                    user.setUserLastLogin(LocalDateTime.now());
                    return userRepository.save(user);
                })
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
    }
}
