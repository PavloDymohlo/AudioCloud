package ua.dymohlo.auth_service.saga.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.dto.response.RegisteredUserInfoResponse;
import ua.dymohlo.auth_service.entiti.User;
import ua.dymohlo.auth_service.exception.PaymentFailedException;
import ua.dymohlo.auth_service.exception.UserAlreadyExistsException;
import ua.dymohlo.auth_service.repository.UserRepository;
import ua.dymohlo.auth_service.saga.entity.SagaState;
import ua.dymohlo.auth_service.saga.models.SagaStatus;

@Service
@Slf4j
@RequiredArgsConstructor
public class SagaOrchestrator {

    private final UserRepository userRepository;
    private final SagaStateManager stateManager;
    private final RegistrationSaga registrationSaga;

    public RegisteredUserInfoResponse register(RegisterRequest request) {
        if (userRepository.findByUserEmail(request.getUserEmail()).isPresent()) {
            throw new UserAlreadyExistsException("This email already exists");
        }

        if (stateManager.isUserRegistrationInProgress(request.getUserEmail())) {
            throw new RuntimeException("Registration already in progress for this email");
        }

        log.info("Starting SAGA registration for user: {}", request.getUserEmail());

        SagaState sagaResult = registrationSaga.executeRegistration(request);

        if (sagaResult.getStatus() == SagaStatus.COMPLETED) {
            log.info("SAGA registration completed successfully for user: {}", request.getUserEmail());

            User user = createUserEntity(sagaResult.getUserId(), request.getUserEmail());

            return RegisteredUserInfoResponse.builder()
                    .user(user)
                    .subscriptionName(sagaResult.getSubscriptionName())
                    .build();
        } else {
            log.error("SAGA registration failed for user: {}, error: {}",
                    request.getUserEmail(), sagaResult.getErrorMessage());
            throw new PaymentFailedException(
                    sagaResult.getErrorMessage() != null ? sagaResult.getErrorMessage() : "Registration failed"
            );
        }
    }

    private User createUserEntity(java.util.UUID userId, String userEmail) {
        return User.builder()
                .id(userId)
                .userEmail(userEmail)
                .userRole(ua.dymohlo.auth_service.models.UserRole.CLIENT)
                .userCreatedAt(java.time.LocalDateTime.now())
                .build();
    }
}