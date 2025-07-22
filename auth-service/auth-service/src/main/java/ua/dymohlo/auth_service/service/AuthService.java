package ua.dymohlo.auth_service.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.dymohlo.auth_service.client.PaymentClient;
import ua.dymohlo.auth_service.dto.request.LoginInRequest;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.dto.response.PaymentResultResponse;
import ua.dymohlo.auth_service.entiti.User;
import ua.dymohlo.auth_service.exception.InvalidCredentialsException;
import ua.dymohlo.auth_service.exception.PaymentFailedException;
import ua.dymohlo.auth_service.exception.UserAlreadyExistsException;
import ua.dymohlo.auth_service.models.UserRole;
import ua.dymohlo.auth_service.repository.UserRepository;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final PaymentClient paymentClient;
    private static final UserRole DEFAULT_USER_ROLE = UserRole.CLIENT;

    public User register(RegisterRequest request) {
        userRepository.findByUserEmail(request.getUserEmail())
                .ifPresent(user -> {
                    throw new UserAlreadyExistsException("This email already exists");
                });

        PaymentResultResponse paymentResult = paymentClient.paymentProcess(request);

        if (!paymentResult.isSuccess()) {
            throw new PaymentFailedException(paymentResult.getMessage());
        }
        log.info("Payment successful for user: {}", request.getUserEmail());

        User user = User.builder()
                .userEmail(request.getUserEmail())
                .userPassword(passwordEncoder.encode(request.getPassword()))
                .userRole(DEFAULT_USER_ROLE)
                .userCreatedAt(LocalDateTime.now())
                .build();

        return userRepository.save(user);
    }

    public User loginIn(LoginInRequest request) {
        return userRepository.findByUserEmail(request.getUserEmail())
                .filter(user -> passwordEncoder.matches(request.getPassword(), user.getUserPassword()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
    }
}
