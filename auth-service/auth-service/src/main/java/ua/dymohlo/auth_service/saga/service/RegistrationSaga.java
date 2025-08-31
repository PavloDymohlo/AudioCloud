package ua.dymohlo.auth_service.saga.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ua.dymohlo.auth_service.client.PaymentClient;
import ua.dymohlo.auth_service.client.SubscriptionClient;
import ua.dymohlo.auth_service.dto.request.RegisterRequest;
import ua.dymohlo.auth_service.dto.response.PaymentResultResponse;
import ua.dymohlo.auth_service.dto.response.SubscriptionResponse;
import ua.dymohlo.auth_service.saga.dto.CreateUserSagaRequest;
import ua.dymohlo.auth_service.saga.dto.NotificationRequest;
import ua.dymohlo.auth_service.saga.entity.SagaState;
import ua.dymohlo.auth_service.saga.models.SagaStatus;
import ua.dymohlo.auth_service.saga.models.SagaStep;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class RegistrationSaga {

    private final SagaStateManager stateManager;
    private final CompensationHandler compensationHandler;
    private final SubscriptionClient subscriptionClient;
    private final PaymentClient paymentClient;
    private final WebClient webClient;
    private final KafkaTemplate<String, NotificationRequest> kafkaTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();
    @Value("${saga.internal-api-key}")
    private String internalApiKey;

    public SagaState executeRegistration(RegisterRequest request, UUID userId) {
        String requestData = serializeRequest(request);

        SagaState sagaState = stateManager.createSaga(userId, request.getUserEmail(), requestData);

        try {
            stateManager.updateStep(sagaState.getSagaId(), SagaStep.SUBSCRIPTION_CHECK, SagaStatus.IN_PROGRESS);
            SubscriptionResponse subscription = checkSubscription();

            stateManager.updateWithData(sagaState.getSagaId(), SagaStep.PAYMENT_PROCESSING,
                    SagaStatus.IN_PROGRESS, null, subscription.getSubscriptionName());
            PaymentResultResponse paymentResult = processPayment(request, subscription);

            if (!paymentResult.isSuccess()) {
                return handleFailure(sagaState.getSagaId(), "Payment failed: " + paymentResult.getMessage());
            }

            stateManager.updateWithData(sagaState.getSagaId(), SagaStep.USER_CREATION,
                    SagaStatus.IN_PROGRESS, paymentResult.getTransactionId(), null);
            boolean userCreated = createUser(userId, request, subscription.getSubscriptionName());

            if (!userCreated) {
                return compensateAndFail(sagaState.getSagaId(), "User creation failed");
            }

            SagaState completedSaga = stateManager.updateStep(sagaState.getSagaId(), SagaStep.COMPLETED, SagaStatus.COMPLETED);

            sendSuccessNotification(completedSaga, request.getUserEmail());

            return completedSaga;

        } catch (Exception e) {
            log.error("SAGA execution failed for sagaId: {}", sagaState.getSagaId(), e);
            return compensateAndFail(sagaState.getSagaId(), e.getMessage());
        }
    }

    private SubscriptionResponse checkSubscription() {
        try {
            String subscriptionJson = subscriptionClient.getDefaultSubscription();
            return objectMapper.readValue(subscriptionJson, SubscriptionResponse.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to get subscription info", e);
        }
    }

    private PaymentResultResponse processPayment(RegisterRequest request, SubscriptionResponse subscription) {
        return paymentClient.paymentProcess(request);
    }

    private boolean createUser(UUID userId, RegisterRequest request, String subscriptionName) {
        try {
            CreateUserSagaRequest userRequest = CreateUserSagaRequest.builder()
                    .userId(userId)
                    .userEmail(request.getUserEmail())
                    .userRole("CLIENT")
                    .subscriptionName(subscriptionName)
                    .bankCardNumber(request.getBankCardNumber())
                    .bankCardNumberCVV(request.getBankCardNumberCVV())
                    .bankCardNumberExpired(request.getBankCardNumberExpired())
                    .build();

            String response = webClient.post()
                    .uri("lb://user-service/api/v1/users/saga")
                    .header("Content-Type", "application/json")
                    .header("X-Internal-API-Key", internalApiKey)
                    .bodyValue(userRequest)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("User created successfully: {}", response);
            return true;

        } catch (Exception e) {
            log.error("Failed to create user: {}", e.getMessage());
            return false;
        }
    }

    private void sendSuccessNotification(SagaState sagaState, String userEmail) {
        try {
            NotificationRequest notification = NotificationRequest.builder()
                    .success(true)
                    .transactionId(sagaState.getPaymentTransactionId())
                    .message("Registration completed successfully")
                    .recipient(userEmail)
                    .notificationType("email")
                    .messageType("REGISTRATION_SUCCESS")
                    .build();

            kafkaTemplate.send("notifications", sagaState.getUserId().toString(), notification);

            log.info("Success notification sent for completed SAGA: sagaId={}, user={}",
                    sagaState.getSagaId(), userEmail);

        } catch (Exception e) {
            log.error("Failed to send success notification for sagaId: {}", sagaState.getSagaId(), e);
        }
    }

    private SagaState handleFailure(UUID sagaId, String errorMessage) {
        return stateManager.markFailed(sagaId, errorMessage);
    }

    private SagaState compensateAndFail(UUID sagaId, String errorMessage) {
        SagaState currentState = stateManager.getSaga(sagaId).orElse(null);
        if (currentState == null) {
            return stateManager.markFailed(sagaId, "SAGA not found during compensation");
        }

        stateManager.updateStep(sagaId, SagaStep.COMPENSATING_PAYMENT, SagaStatus.COMPENSATING);

        boolean paymentCompensated = true;
        if (currentState.getPaymentTransactionId() != null) {
            paymentCompensated = compensationHandler.compensatePayment(currentState);
        }

        boolean userCompensated = true;
        if (currentState.getCurrentStep() == SagaStep.USER_CREATION) {
            userCompensated = compensationHandler.compensateUser(currentState);
        }

        if (paymentCompensated && userCompensated) {
            stateManager.updateStep(sagaId, SagaStep.COMPENSATION_COMPLETED, SagaStatus.FAILED);
        }

        return stateManager.markFailed(sagaId, errorMessage);
    }

    private String serializeRequest(RegisterRequest request) {
        try {
            return objectMapper.writeValueAsString(request);
        } catch (Exception e) {
            log.warn("Failed to serialize request", e);
            return "{}";
        }
    }
}