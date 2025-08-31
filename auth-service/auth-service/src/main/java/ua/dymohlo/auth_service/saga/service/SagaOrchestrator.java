package ua.dymohlo.auth_service.saga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.dto.response.RegisteredUserInfoResponse;
import ua.dymohlo.auth_service.entiti.User;
import ua.dymohlo.auth_service.exception.PaymentFailedException;
import ua.dymohlo.auth_service.exception.UserAlreadyExistsException;
import ua.dymohlo.auth_service.models.UserRole;
import ua.dymohlo.auth_service.repository.UserRepository;
import ua.dymohlo.auth_service.saga.entity.SagaState;
import ua.dymohlo.auth_service.saga.models.SagaStatus;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final UserRepository userRepository;
    private final SagaStateManager stateManager;
    private final RegistrationSaga registrationSaga;
    private final PasswordEncoder passwordEncoder;

    public RegisteredUserInfoResponse register(RegisterRequest request) {
        if (userRepository.findByUserEmail(request.getUserEmail()).isPresent()) {
            throw new UserAlreadyExistsException("This email already exists");
        }

        if (stateManager.isUserRegistrationInProgress(request.getUserEmail())) {
            throw new RuntimeException("Registration already in progress for this email");
        }

        log.info("Starting SAGA registration for user: {}", request.getUserEmail());
        User authUser = createAuthUser(request);
        UUID userId = authUser.getId();

        log.info("User created in auth-service: userId={}, email={}", userId, request.getUserEmail());

        try {
            SagaState sagaResult = registrationSaga.executeRegistration(request, userId);

            if (sagaResult.getStatus() == SagaStatus.COMPLETED) {
                log.info("SAGA registration completed successfully for user: {}", request.getUserEmail());

                return RegisteredUserInfoResponse.builder()
                        .user(authUser)
                        .subscriptionName(sagaResult.getSubscriptionName())
                        .build();
            } else {
                log.error("SAGA registration failed, removing user from auth-service: {}", request.getUserEmail());
                userRepository.delete(authUser);

                throw new PaymentFailedException(
                        sagaResult.getErrorMessage() != null ? sagaResult.getErrorMessage() : "Registration failed"
                );
            }
        } catch (Exception e) {
            log.error("SAGA execution failed, removing user from auth-service: {}", request.getUserEmail(), e);
            userRepository.delete(authUser);
            throw new RuntimeException("Registration failed: " + e.getMessage(), e);
        }
    }

    private User createAuthUser(RegisterRequest request) {
        try {
            User user = User.builder()
                    .userEmail(request.getUserEmail())
                    .userPassword(passwordEncoder.encode(request.getPassword()))
                    .userRole(UserRole.CLIENT)
                    .userCreatedAt(LocalDateTime.now())
                    .build();

            User savedUser = userRepository.save(user);
            log.info("User saved to auth-service database: userId={}, email={}",
                    savedUser.getId(), savedUser.getUserEmail());

            return savedUser;
        } catch (Exception e) {
            log.error("Failed to save user to auth-service: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save user to auth-service", e);
        }
    }
}